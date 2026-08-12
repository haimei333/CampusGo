# CampusGo 数据表结构设计文档

> **版本**: v1.1  
> **日期**: 2026-07-27  
> **状态**: 初稿  
> **数据库引擎**: **PostgreSQL 16+**（推荐；最低建议 14+）  
> **依据**: `CampusGo-PRD.md`、`CampusGo-界面拆分.md`、`CampusGo-技术架构文档.md`  
> **金额约定**: 一律用 **分（INTEGER）** 存储，避免浮点误差；接口层再格式化为元  
> **说明**: 本文类型、索引、锁语法均按 PostgreSQL 书写；不再以 MySQL 为默认引擎  

---

## 目录

1. [设计原则](#1-设计原则)
2. [库与模块总览](#2-库与模块总览)
3. [枚举与状态约定](#3-枚举与状态约定)
4. [一期表结构（P0）](#4-一期表结构p0)
5. [二期表结构（P1）](#5-二期表结构p1)
6. [三期表结构（P2）](#6-三期表结构p2)
7. [索引与并发要点](#7-索引与并发要点)
8. [客户端本地库（Room）](#8-客户端本地库room)
9. [ER 关系简图](#9-er-关系简图)
10. [修订记录](#10-修订记录)

---

## 1. 设计原则


| 原则    | 说明                                                                            |
| ----- | ----------------------------------------------------------------------------- |
| 单一业务库 | 数据库（database）名建议 `campusgo`；按模块逻辑分表，不拆多库                                      |
| 服务端权威 | 任务状态、余额、信用分以服务端表为准                                                            |
| 软删除慎用 | 账务、订单类用状态字段；用户注销走合规删除/匿名化流程                                                   |
| 金额用分  | `amount_cent`、`balance_cent` 等，类型 `INTEGER`                                   |
| 坐标    | 高德坐标系，`NUMERIC(10,7)` 存经纬度；远期距离查询可扩展 **PostGIS**（非一期必选）                       |
| 主键    | 默认 `BIGSERIAL`（`BIGINT` 自增）；对外可另发 `biz_no` / `task_no` 业务单号                   |
| 时间    | 统一 `TIMESTAMPTZ`（带时区）+ `created_at` / `updated_at`，默认 `NOW()`                 |
| 编码    | 数据库编码 `UTF8`（PostgreSQL 默认）                                                   |
| JSON  | 标签、举证等多值字段优先 **JSONB**                                                        |
| 表名    | `user` 为保留字，建表时使用双引号 `"user"`，或改用 `app_user`（本文字段说明仍称 user 表，落地推荐 `app_user`） |


### 1.1 PostgreSQL 类型对照（相对常见 MySQL 写法）


| 本文（PostgreSQL）        | 常见 MySQL 写法             | 说明                               |
| --------------------- | ----------------------- | -------------------------------- |
| `BIGSERIAL`           | `BIGINT AUTO_INCREMENT` | 主键自增                             |
| `INTEGER`             | `INT`                   | 整数 / 金额（分）                       |
| `VARCHAR(n)` / `TEXT` | `VARCHAR` / `TEXT`      | 字符串                              |
| `NUMERIC(10,7)`       | `NUMERIC(10,7)`         | 精确小数                             |
| `TIMESTAMPTZ`         | `DATETIME`              | 建议带时区                            |
| `JSONB`               | `JSON`                  | 可索引、查询更强                         |
| `BOOLEAN`             | `SMALLINT(1)`           | 可用；本文部分仍用 `SMALLINT` 0/1 与既有习惯兼容 |


---

## 2. 库与模块总览

```
campusgo（PostgreSQL）
├── 用户与认证     app_user / user_auth_campus / sms_code
├── 任务履约       task / task_status_log / task_location_point
├── 地址模板       user_address / task_template（模板可二期）
├── 资金           wallet / wallet_ledger / withdraw_account / withdraw_order
├── 消息通知       chat_conversation / chat_message / notification
├── 评价投诉       review / complaint（投诉可二期）
└── 运营增长       points_* / coupon_*（三期）

辅助（非 PostgreSQL 表，但要规划）
├── Redis          验证码、限流、抢单锁辅助
└── OSS            图片文件（表内只存 URL）
```


| 优先级         | 模块                        | 表   |
| ----------- | ------------------------- | --- |
| **P0 一期必建** | 用户、认证、任务、地址、钱包、提现、消息、通知   | §4  |
| **P1 二期**   | 拼单成员细化、预约、评价、投诉、模板、加价流水细化 | §5  |
| **P2 三期**   | 积分、签到、优惠券                 | §6  |


---

## 3. 枚举与状态约定

### 3.1 任务状态 `task.status`（全平台唯一）


| 存库值          | 含义  | 说明          |
| ------------ | --- | ----------- |
| `DRAFT`      | 草稿  | 未提交托管       |
| `GROUPING`   | 拼单中 | 未满员         |
| `RESERVING`  | 预约中 | 未到公开/接单点    |
| `PENDING`    | 待接单 | 可抢单         |
| `ACCEPTED`   | 已接单 | 已锁定跑腿员      |
| `DELIVERING` | 配送中 | 可追踪位置       |
| `CONFIRMING` | 待确认 | 已拍照，等发布者    |
| `COMPLETED`  | 已完成 | 已结算         |
| `REVIEWED`   | 已评价 | 双方评完或超时默认好评 |
| `CANCELLED`  | 已取消 | 终态          |


### 3.2 任务模式 `task.mode`


| 值           | 含义  |
| ----------- | --- |
| `NORMAL`    | 普通  |
| `GROUP`     | 拼单  |
| `EMERGENCY` | 紧急  |
| `RESERVE`   | 预约  |


### 3.3 服务分类 `task.category`


| 值         | 含义   |
| --------- | ---- |
| `EXPRESS` | 代取快递 |
| `BUY`     | 代买物品 |
| `ERRAND`  | 代办事务 |


### 3.4 其他常用枚举


| 字段                      | 取值                                                                                                                                             |
| ----------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| `user.active_role`      | `PUBLISHER` / `RUNNER`                                                                                                                         |
| `wallet_ledger.type`    | `TOPUP` / `ESCROW_HOLD` / `ESCROW_RELEASE` / `ESCROW_REFUND` / `INCOME` / `WITHDRAW_FREEZE` / `WITHDRAW_SUCCESS` / `WITHDRAW_REJECT` / `RAISE` |
| `withdraw_order.status` | `PENDING` / `APPROVED` / `PAID` / `REJECTED`                                                                                                   |
| `campus_verify.status`  | `NONE` / `PENDING` / `APPROVED` / `REJECTED`                                                                                                   |
| `chat_message.msg_type` | `TEXT` / `IMAGE` / `VOICE` / `SYSTEM`                                                                                                          |
| `notification.biz_type` | 见界面拆分通知深链表                                                                                                                                     |


---

## 4. 一期表结构（P0）

### 4.1 `app_user` — 用户（落地表名；下文逻辑仍称「用户表」）

> PostgreSQL 中 `user` 为保留字，**建表请使用 `app_user`**，避免强制引用 `"user"`。


| 字段            | 类型           | 空   | 默认        | 说明           |
| ------------- | ------------ | --- | --------- | ------------ |
| id            | BIGSERIAL PK | N   |           | 用户 ID        |
| phone         | VARCHAR(20)  | N   |           | 手机号，唯一       |
| nickname      | VARCHAR(32)  | N   |           | 昵称           |
| avatar_url    | VARCHAR(512) | Y   | NULL      | 头像           |
| credit_score  | INTEGER      | N   | 500       | 信用分 0–1000   |
| active_role   | VARCHAR(16)  | N   | PUBLISHER | 当前身份         |
| campus_status | VARCHAR(16)  | N   | NONE      | 认证状态冗余，便于查询  |
| status        | SMALLINT     | N   | 1         | 1正常 0禁用 2注销中 |
| created_at    | TIMESTAMPTZ  | N   | NOW()     |              |
| updated_at    | TIMESTAMPTZ  | N   | NOW()     |              |


**索引**

- `uk_user_phone` UNIQUE(`phone`)
- `idx_user_credit` (`credit_score`)

---

### 4.2 `sms_code` — 短信验证码（可改 Redis，表可选）


| 字段         | 类型           | 空   | 说明             |
| ---------- | ------------ | --- | -------------- |
| id         | BIGSERIAL PK | N   |                |
| phone      | VARCHAR(20)  | N   |                |
| code       | VARCHAR(8)   | N   |                |
| scene      | VARCHAR(32)  | N   | LOGIN / REBIND |
| expired_at | TIMESTAMPTZ  | N   |                |
| used       | SMALLINT     | N   | 0未用 1已用        |
| created_at | TIMESTAMPTZ  | N   |                |


**索引**: `idx_sms_phone_scene` (`phone`, `scene`, `created_at`)

---

### 4.3 `user_auth_campus` — 校园卡认证


| 字段             | 类型           | 空   | 说明                        |
| -------------- | ------------ | --- | ------------------------- |
| id             | BIGSERIAL PK | N   |                           |
| user_id        | BIGINT       | N   | FK → app_user.id          |
| real_name      | VARCHAR(64)  | Y   | OCR/手工                    |
| student_no     | VARCHAR(64)  | Y   | 学号                        |
| college        | VARCHAR(128) | Y   | 学院                        |
| card_image_url | VARCHAR(512) | N   | 证件照 OSS                   |
| status         | VARCHAR(16)  | N   | PENDING/APPROVED/REJECTED |
| reject_reason  | VARCHAR(256) | Y   |                           |
| audited_at     | TIMESTAMPTZ  | Y   |                           |
| created_at     | TIMESTAMPTZ  | N   |                           |
| updated_at     | TIMESTAMPTZ  | N   |                           |


**索引**

- `uk_campus_user` UNIQUE(`user_id`)（一用户一条当前认证）
- `uk_campus_student_no` UNIQUE(`student_no`)（学号不可重复；PostgreSQL 允许多个 `NULL`，未填学号不冲突）

---

### 4.4 `user_address` — 常用地址


| 字段         | 类型            | 空   | 说明                                  |
| ---------- | ------------- | --- | ----------------------------------- |
| id         | BIGSERIAL PK  | N   |                                     |
| user_id    | BIGINT        | N   |                                     |
| name       | VARCHAR(40)   | N   | 如「宿舍楼下」                             |
| detail     | VARCHAR(256)  | N   | 详细地址                                |
| lng        | NUMERIC(10,7) | N   | 高德经度                                |
| lat        | NUMERIC(10,7) | N   | 高德纬度                                |
| tag        | VARCHAR(16)   | Y   | DORM/TEACHING/CANTEEN/EXPRESS/OTHER |
| use_count  | INTEGER       | N   | 0                                   |
| created_at | TIMESTAMPTZ   | N   |                                     |
| updated_at | TIMESTAMPTZ   | N   |                                     |


**索引**: `idx_addr_user` (`user_id`)  
**约束**: 每用户建议 ≤ 30 条（应用层校验）

---

### 4.5 `task` — 任务（核心表）


| 字段                 | 类型            | 空   | 说明                             |
| ------------------ | ------------- | --- | ------------------------------ |
| id                 | BIGSERIAL PK  | N   |                                |
| task_no            | VARCHAR(32)   | N   | 业务单号，唯一，如 CG20240725001        |
| publisher_id       | BIGINT        | N   | 发布者                            |
| runner_id          | BIGINT        | Y   | 跑腿员，接单后写入                      |
| mode               | VARCHAR(16)   | N   | NORMAL/GROUP/EMERGENCY/RESERVE |
| category           | VARCHAR(16)   | N   | EXPRESS/BUY/ERRAND             |
| title              | VARCHAR(60)   | N   | 2–30 字                         |
| description        | VARCHAR(400)  | Y   |                                |
| status             | VARCHAR(16)   | N   | 见 §3.1                         |
| pickup_name        | VARCHAR(128)  | N   | 取件点名称                          |
| pickup_detail      | VARCHAR(256)  | Y   |                                |
| pickup_lng         | NUMERIC(10,7) | N   |                                |
| pickup_lat         | NUMERIC(10,7) | N   |                                |
| dropoff_name       | VARCHAR(128)  | N   | 送达点                            |
| dropoff_detail     | VARCHAR(256)  | Y   |                                |
| dropoff_lng        | NUMERIC(10,7) | N   |                                |
| dropoff_lat        | NUMERIC(10,7) | N   |                                |
| expect_finish_at   | TIMESTAMPTZ   | Y   | 期望完成时间                         |
| reserve_at         | TIMESTAMPTZ   | Y   | 预约任务生效时间                       |
| reward_cent        | INTEGER       | N   | 当前应付酬劳（含加价后）                   |
| base_reward_cent   | INTEGER       | N   | 基础酬劳                           |
| emergency_rate     | INTEGER       | Y   | 紧急加价比例，如 50 表示 50%             |
| escrow_cent        | INTEGER       | N   | 已托管金额                          |
| group_target_count | INTEGER       | Y   | 拼单目标人数 2–10                    |
| group_joined_count | INTEGER       | Y   | 已加入人数                          |
| group_split_type   | VARCHAR(16)   | Y   | EQUAL / DISTANCE               |
| coupon_id          | BIGINT        | Y   | 使用的优惠券（三期）                     |
| delivery_photo_url | VARCHAR(512)  | Y   | 送达照片                           |
| cancel_reason      | VARCHAR(256)  | Y   |                                |
| accepted_at        | TIMESTAMPTZ   | Y   |                                |
| delivering_at      | TIMESTAMPTZ   | Y   |                                |
| confirming_at      | TIMESTAMPTZ   | Y   |                                |
| completed_at       | TIMESTAMPTZ   | Y   |                                |
| cancelled_at       | TIMESTAMPTZ   | Y   |                                |
| version            | INTEGER       | N   | 乐观锁，抢单用                        |
| created_at         | TIMESTAMPTZ   | N   |                                |
| updated_at         | TIMESTAMPTZ   | N   |                                |


**索引**

- `uk_task_no` UNIQUE(`task_no`)
- `idx_task_publisher_status` (`publisher_id`, `status`)
- `idx_task_runner_status` (`runner_id`, `status`)
- `idx_task_hall` (`status`, `mode`, `created_at`) — 大厅列表
- `idx_task_geo_pending` (`status`, `pickup_lng`, `pickup_lat`) — 可选，距离筛选可应用层算

**抢单说明**：更新时 `WHERE id=? AND status='PENDING' AND runner_id IS NULL`，配合 `version` 或行锁，保证不超抢。

---

### 4.6 `task_status_log` — 任务状态流水


| 字段          | 类型           | 空   | 说明  |
| ----------- | ------------ | --- | --- |
| id          | BIGSERIAL PK | N   |     |
| task_id     | BIGINT       | N   |     |
| from_status | VARCHAR(16)  | Y   |     |
| to_status   | VARCHAR(16)  | N   |     |
| operator_id | BIGINT       | Y   | 操作人 |
| remark      | VARCHAR(256) | Y   |     |
| created_at  | TIMESTAMPTZ  | N   |     |


**索引**: `idx_tsl_task` (`task_id`, `created_at`)

---

### 4.7 `task_location_point` — 配送轨迹点


| 字段          | 类型            | 空   | 说明        |
| ----------- | ------------- | --- | --------- |
| id          | BIGSERIAL PK  | N   |           |
| task_id     | BIGINT        | N   |           |
| runner_id   | BIGINT        | N   |           |
| lng         | NUMERIC(10,7) | N   |           |
| lat         | NUMERIC(10,7) | N   |           |
| signal_lost | SMALLINT      | N   | 0正常 1信号丢失 |
| recorded_at | TIMESTAMPTZ   | N   | 采集时间      |


**索引**: `idx_loc_task_time` (`task_id`, `recorded_at`)  
**策略**: 任务结束后可归档/脱敏；列表页不查全量轨迹。

---

### 4.8 `wallet` — 钱包账户


| 字段                  | 类型           | 空   | 说明     |
| ------------------- | ------------ | --- | ------ |
| id                  | BIGSERIAL PK | N   |        |
| user_id             | BIGINT       | N   | UNIQUE |
| balance_cent        | INTEGER      | N   | 可用余额   |
| frozen_cent         | INTEGER      | N   | 提现冻结等  |
| total_income_cent   | INTEGER      | N   | 累计收入   |
| total_withdraw_cent | INTEGER      | N   | 累计提现   |
| updated_at          | TIMESTAMPTZ  | N   |        |
| created_at          | TIMESTAMPTZ  | N   |        |


**索引**: `uk_wallet_user` UNIQUE(`user_id`)

---

### 4.9 `wallet_ledger` — 资金流水


| 字段                 | 类型           | 空   | 说明                                  |
| ------------------ | ------------ | --- | ----------------------------------- |
| id                 | BIGSERIAL PK | N   |                                     |
| ledger_no          | VARCHAR(32)  | N   | 流水号唯一                               |
| user_id            | BIGINT       | N   |                                     |
| type               | VARCHAR(32)  | N   | 见 §3.4                              |
| amount_cent        | INTEGER      | N   | 正为入账方向约定：以 type 解释；建议另加 `direction` |
| direction          | SMALLINT     | N   | 1入账 -1出账                            |
| balance_after_cent | INTEGER      | N   | 变动后可用余额                             |
| task_id            | BIGINT       | Y   | 关联任务                                |
| ref_no             | VARCHAR(64)  | Y   | 支付渠道单号/提现单号                         |
| remark             | VARCHAR(256) | Y   |                                     |
| created_at         | TIMESTAMPTZ  | N   |                                     |


**索引**

- `uk_ledger_no` UNIQUE(`ledger_no`)
- `idx_ledger_user_time` (`user_id`, `created_at`)
- `idx_ledger_task` (`task_id`)

---

### 4.10 `withdraw_account` — 提现账户


| 字段             | 类型           | 空   | 说明                     |
| -------------- | ------------ | --- | ---------------------- |
| id             | BIGSERIAL PK | N   |                        |
| user_id        | BIGINT       | N   |                        |
| channel        | VARCHAR(16)  | N   | WECHAT / ALIPAY / BANK |
| account_mask   | VARCHAR(64)  | N   | 脱敏展示                   |
| account_secret | VARCHAR(256) | N   | 加密存储真实账号               |
| real_name      | VARCHAR(64)  | Y   |                        |
| is_default     | SMALLINT     | N   | 0/1                    |
| created_at     | TIMESTAMPTZ  | N   |                        |
| updated_at     | TIMESTAMPTZ  | N   |                        |


**索引**: `idx_wa_user` (`user_id`)

---

### 4.11 `withdraw_order` — 提现申请


| 字段            | 类型           | 空   | 说明                             |
| ------------- | ------------ | --- | ------------------------------ |
| id            | BIGSERIAL PK | N   |                                |
| order_no      | VARCHAR(32)  | N   | UNIQUE                         |
| user_id       | BIGINT       | N   |                                |
| account_id    | BIGINT       | N   |                                |
| amount_cent   | INTEGER      | N   | 1 元～500 元                      |
| status        | VARCHAR(16)  | N   | PENDING/APPROVED/PAID/REJECTED |
| reject_reason | VARCHAR(256) | Y   |                                |
| audited_at    | TIMESTAMPTZ  | Y   |                                |
| paid_at       | TIMESTAMPTZ  | Y   |                                |
| created_at    | TIMESTAMPTZ  | N   |                                |
| updated_at    | TIMESTAMPTZ  | N   |                                |


**索引**: `idx_wo_user_time` (`user_id`, `created_at`)  
**规则**: 每日 ≤ 3 次（应用层 + 可选日汇总表）

---

### 4.12 `chat_conversation` — 聊天会话


| 字段               | 类型           | 空   | 说明           |
| ---------------- | ------------ | --- | ------------ |
| id               | BIGSERIAL PK | N   |              |
| task_id          | BIGINT       | N   | UNIQUE，一单一会话 |
| publisher_id     | BIGINT       | N   |              |
| runner_id        | BIGINT       | N   |              |
| last_msg_preview | VARCHAR(200) | Y   |              |
| last_msg_at      | TIMESTAMPTZ  | Y   |              |
| archived         | SMALLINT     | N   | 完成后 7 天归档    |
| created_at       | TIMESTAMPTZ  | N   |              |
| updated_at       | TIMESTAMPTZ  | N   |              |


**索引**: `uk_conv_task` UNIQUE(`task_id`)；`idx_conv_user_time` 可用冗余用户会话表或查询两边 OR

---

### 4.13 `chat_message` — 聊天消息


| 字段              | 类型            | 空   | 说明                      |
| --------------- | ------------- | --- | ----------------------- |
| id              | BIGSERIAL PK  | N   |                         |
| conversation_id | BIGINT        | N   |                         |
| task_id         | BIGINT        | N   | 冗余便于查询                  |
| sender_id       | BIGINT        | N   |                         |
| msg_type        | VARCHAR(16)   | N   | TEXT/IMAGE/VOICE/SYSTEM |
| content         | VARCHAR(2000) | N   | 文本或资源 URL               |
| read_flag       | SMALLINT      | N   | 对方是否已读（简化）              |
| created_at      | TIMESTAMPTZ   | N   |                         |


**索引**: `idx_msg_conv_time` (`conversation_id`, `id`)

---

### 4.14 `notification` — 系统通知


| 字段         | 类型           | 空   | 说明                                          |
| ---------- | ------------ | --- | ------------------------------------------- |
| id         | BIGSERIAL PK | N   |                                             |
| user_id    | BIGINT       | N   | 接收人                                         |
| title      | VARCHAR(128) | N   |                                             |
| body       | VARCHAR(512) | N   |                                             |
| biz_type   | VARCHAR(32)  | N   | TASK_ACCEPTED / CONFIRM / REVIEW / APPEAL … |
| biz_id     | VARCHAR(64)  | Y   | 任务 ID 等                                     |
| deeplink   | VARCHAR(256) | Y   | 客户端路由                                       |
| read_flag  | SMALLINT     | N   | 0/1                                         |
| created_at | TIMESTAMPTZ  | N   |                                             |


**索引**: `idx_notif_user_time` (`user_id`, `created_at`)

---

### 4.15 一期表清单汇总


| 表名                  | 说明           |
| ------------------- | ------------ |
| app_user            | 用户           |
| sms_code            | 验证码（可 Redis） |
| user_auth_campus    | 校园卡认证        |
| user_address        | 常用地址         |
| task                | 任务           |
| task_status_log     | 状态流水         |
| task_location_point | 轨迹点          |
| wallet              | 钱包           |
| wallet_ledger       | 资金流水         |
| withdraw_account    | 提现账户         |
| withdraw_order      | 提现单          |
| chat_conversation   | 会话           |
| chat_message        | 消息           |
| notification        | 通知           |


---

## 5. 二期表结构（P1）

### 5.1 `task_group_member` — 拼单成员


| 字段                   | 类型           | 说明                         |
| -------------------- | ------------ | -------------------------- |
| id                   | BIGSERIAL PK |                            |
| task_id              | BIGINT       | 拼单主任务                      |
| user_id              | BIGINT       | 成员                         |
| role                 | VARCHAR(16)  | OWNER / MEMBER             |
| pickup_* / dropoff_* |              | 成员自己的地址                    |
| share_cent           | INTEGER      | 分摊金额                       |
| pay_status           | VARCHAR(16)  | UNPAID/PAID/REFUNDED       |
| delivery_photo_url   | VARCHAR(512) | 该成员送达照                     |
| confirm_status       | VARCHAR(16)  | PENDING/CONFIRMED/DISPUTED |
| joined_at            | TIMESTAMPTZ  |                            |


**索引**: `uk_group_task_user` UNIQUE(`task_id`, `user_id`)

---

### 5.2 `task_reserve_slot` — 预约占位


| 字段                         | 类型           | 说明                                         |
| -------------------------- | ------------ | ------------------------------------------ |
| id                         | BIGSERIAL PK |                                            |
| task_id                    | BIGINT       |                                            |
| runner_id                  | BIGINT       |                                            |
| status                     | VARCHAR(16)  | HOLDING / CONFIRMED / RELEASED / CANCELLED |
| hold_at / confirm_deadline | TIMESTAMPTZ  |                                            |
| created_at                 | TIMESTAMPTZ  |                                            |


**索引**: `uk_reserve_task_runner` UNIQUE(`task_id`, `runner_id`)；同任务占位人数应用层 ≤ 5

---

### 5.3 `task_template` — 任务模板


| 字段                      | 类型           | 说明              |
| ----------------------- | ------------ | --------------- |
| id                      | BIGSERIAL PK |                 |
| user_id                 | BIGINT       | NULL=系统模板       |
| name                    | VARCHAR(20)  |                 |
| type                    | VARCHAR(16)  | SYSTEM / CUSTOM |
| category                | VARCHAR(16)  |                 |
| content_json            | JSONB        | 预填字段            |
| use_count               | INTEGER      |                 |
| created_at / updated_at | TIMESTAMPTZ  |                 |


---

### 5.4 `review` — 评价


| 字段           | 类型           | 说明     |
| ------------ | ------------ | ------ |
| id           | BIGSERIAL PK |        |
| task_id      | BIGINT       |        |
| from_user_id | BIGINT       |        |
| to_user_id   | BIGINT       |        |
| score        | SMALLINT     | 1–5    |
| tags_json    | JSONB        | 标签数组   |
| content      | VARCHAR(400) |        |
| is_default   | SMALLINT     | 超时默认好评 |
| created_at   | TIMESTAMPTZ  |        |


**索引**: `uk_review_task_from` UNIQUE(`task_id`, `from_user_id`)

---

### 5.5 `complaint` — 投诉/申诉


| 字段                       | 类型            | 说明                                         |
| ------------------------ | ------------- | ------------------------------------------ |
| id                       | BIGSERIAL PK  |                                            |
| task_id                  | BIGINT        |                                            |
| complainant_id           | BIGINT        | 发起方                                        |
| respondent_id            | BIGINT        | 被投诉方                                       |
| type                     | VARCHAR(32)   | TIMEOUT/DAMAGE/NOT_RECEIVED/ATTITUDE/OTHER |
| content                  | VARCHAR(1000) |                                            |
| evidence_urls            | JSONB         | 最多 3 张图                                    |
| status                   | VARCHAR(16)   | PROCESSING/JUDGED/REJECTED                 |
| appeal_content           | VARCHAR(1000) | 申诉说明                                       |
| appeal_evidence_urls     | JSONB         |                                            |
| appeal_deadline          | TIMESTAMPTZ   |                                            |
| verdict                  | VARCHAR(32)   | COMPLAINANT_WIN / RESPONDENT_WIN / BOTH    |
| verdict_remark           | VARCHAR(512)  |                                            |
| credit_delta_complainant | INTEGER       |                                            |
| credit_delta_respondent  | INTEGER       |                                            |
| created_at / updated_at  | TIMESTAMPTZ   |                                            |


---

### 5.6 `admin_user`（可选）— 后台审核账号

用于校园卡审核、提现审核、投诉仲裁。字段：`id/username/password_hash/role/created_at`。

---

## 6. 三期表结构（P2）

### 6.1 `points_account` / `points_ledger` / `check_in_record`

- 积分余额、流水（签到/任务/扣减）  
- 签到：`user_id + check_date` 唯一；连续天数可冗余在账户表

### 6.2 `coupon_template` / `user_coupon`

- 券模板：满减规则、积分兑换价、库存  
- 用户券：状态 UNUSED/USED/EXPIRED，发布时绑定 `task.coupon_id`

### 6.3 统计

看板可用定时任务写 `stats_daily_user`，或对一期表做聚合查询；初期可不建独立宽表。

---

## 7. 索引与并发要点（PostgreSQL）


| 场景   | 做法                                                                                                                                                                                                                 |
| ---- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 抢单   | 事务内：`UPDATE task SET runner_id=$1, status='ACCEPTED', version=version+1, updated_at=NOW() WHERE id=$2 AND status='PENDING' AND runner_id IS NULL`；`GET DIAGNOSTICS` / 检查影响行数，为 0 则失败。亦可先 `SELECT … FOR UPDATE` 再更新 |
| 扣余额  | 事务内对 `wallet` 行：`SELECT … FOR UPDATE`，校验 `balance_cent >= amount` 后扣减并写 `wallet_ledger`                                                                                                                            |
| 托管释放 | 任务置 `COMPLETED` 的同一事务内：写 ledger（托管释放 + 跑腿员收入）并改余额                                                                                                                                                                  |
| 提现   | 先增加 `frozen_cent` 并写流水；审核通过再扣减；驳回则解冻                                                                                                                                                                               |
| 大厅列表 | `WHERE status='PENDING' ORDER BY created_at DESC` + `LIMIT/OFFSET` 或 keyset 分页                                                                                                                                     |
| 连接串  | JDBC：`jdbc:postgresql://host:5432/campusgo`                                                                                                                                                                        |


**示例（抢单）**：

```sql
BEGIN;
UPDATE task
SET runner_id = $1,
    status = 'ACCEPTED',
    accepted_at = NOW(),
    version = version + 1,
    updated_at = NOW()
WHERE id = $2
  AND status = 'PENDING'
  AND runner_id IS NULL;
-- 若 rowcount = 0：ROLLBACK 并返回「手慢无」
COMMIT;
```

---

## 8. 客户端本地库（Room）

**不是业务主库**，仅建议：


| 表             | 说明                                        |
| ------------- | ----------------------------------------- |
| `local_draft` | 发布草稿 JSON，可与服务端 DRAFT 同步                  |
| `local_prefs` | 也可用 DataStore：token、guideShown、activeRole |


勿在 Room 中单独维护「真余额 / 真任务状态」作为权威数据。

---

## 9. ER 关系简图

```
app_user 1──1 wallet
 app_user 1──1 user_auth_campus
 app_user 1──n user_address
 app_user 1──n task（作为 publisher）
 app_user 1──n task（作为 runner）
 task 1──n task_status_log
 task 1──n task_location_point
 task 1──1 chat_conversation
 chat_conversation 1──n chat_message
 app_user 1──n wallet_ledger
 app_user 1──n withdraw_account
 app_user 1──n withdraw_order
 app_user 1──n notification

（二期）
 task 1──n task_group_member
 task 1──n task_reserve_slot
 task 1──n review
 task 1──n complaint
```

---

## 10. 修订记录


| 版本   | 日期         | 说明                                                                                                    |
| ---- | ---------- | ----------------------------------------------------------------------------------------------------- |
| v1.0 | 2026-07-27 | 首版：一期 14 张核心表 + 二期/三期扩展；金额用分；状态对齐 PRD（原按 MySQL 描述）                                                    |
| v1.1 | 2026-07-27 | **改为 PostgreSQL 为准**：`BIGSERIAL` / `TIMESTAMPTZ` / `NUMERIC` / `JSONB`；用户表落地名 `app_user`；补充 PG 锁与抢单示例 |


---

## 附：一期建表优先级（实施顺序）

1. `app_user` → `wallet`
2. `task` → `task_status_log`
3. `wallet_ledger` → `withdraw_account` → `withdraw_order`
4. `user_auth_campus` → `user_address`
5. `chat_conversation` → `chat_message` → `notification`
6. `task_location_point`（接配送时再上）
7. `sms_code` 或 Redis

---

> **文档结束** — 可与 `CampusGo-技术架构文档.md` 配套使用；表字段落地时以本文件为准生成 **Flyway / Liquibase / 纯 SQL（PostgreSQL）**。若需，可再补一版可直接执行的 `schema_v1_pg.sql`。

