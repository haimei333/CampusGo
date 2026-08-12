# CampusGo 后端架构文档

> **版本**: v1.0  
> **日期**: 2026-07-31  
> **状态**: 初稿  
> **依据文档**:
>
> - `CampusGo-PRD.md` v1.0
> - `CampusGo-数据表结构设计.md` v1.1（PostgreSQL）
> - `CampusGo-技术架构文档.md` v1.1（Android 客户端）
> - `CampusGo-界面拆分.md` / `CampusGo-功能拆解与交互规格.md`
> - Android MVP 现状（`android/` Mock Repository 层）

---

## 目录

1. [文档目标与范围](#1-文档目标与范围)
2. [架构总览](#2-架构总览)
3. [技术选型](#3-技术选型)
4. [服务划分与模块边界](#4-服务划分与模块边界)
5. [API 设计规范](#5-api-设计规范)
6. [认证与会话](#6-认证与会话)
7. [核心领域服务](#7-核心领域服务)
8. [关键业务流程](#8-关键业务流程)
9. [数据架构](#9-数据架构)
10. [基础设施与第三方](#10-基础设施与第三方)
11. [安全与合规](#11-安全与合规)
12. [非功能性要求](#12-非功能性要求)
13. [工程结构与部署](#13-工程结构与部署)
14. [分期交付计划](#14-分期交付计划)
15. [Android 客户端对接映射](#15-android-客户端对接映射)
16. [风险与决策记录](#16-风险与决策记录)
17. [附录](#17-附录)

---

## 1. 文档目标与范围

### 1.1 目标

定义 CampusGo **业务后端**的整体架构，作为服务端开发、Android 联调、运维部署的统一依据。本文侧重 **「怎么建后端」**，库表字段细节以 `CampusGo-数据表结构设计.md` 为准。

### 1.2 范围


| 在范围内 | 不在范围内（可另立文档） |
| -------- | ------------------------ |
| 后端服务划分、API 边界、核心流程 | 运营后台 UI 详细设计 |
| 技术选型、部署拓扑、安全策略 | iOS / 小程序客户端 |
| 与 Android Mock 的对接路径 | 具体 OpenAPI YAML 全量字段（联调前单独产出） |
| P0/P1/P2 分期与 MVP 定义 | 机器学习推荐、复杂 BI |

### 1.3 与现有文档的关系


| 文档 | 角色 |
| ---- | ---- |
| PRD | 业务规则、状态机语义、信用分规则 |
| 数据表结构 | PostgreSQL 表、索引、并发锁 |
| Android 技术架构 | 客户端分层、Retrofit 接入方式 |
| **本文** | 服务端模块、API、流程、部署 |

### 1.4 设计原则

1. **服务端权威**：任务状态、余额、抢单结果、信用分以服务端为准。
2. **金额用分**：库内 `INTEGER` 存分；API JSON 可传 `amountCent` 或格式化的 `amountYuan`（二选一并全局统一）。
3. **先单体、后拆分**：MVP 采用 **模块化单体（Modular Monolith）**，避免过早微服务。
4. **与 Android 枚举对齐**：`TaskStatus`、`TaskMode`、`TaskCategory`、`UserRole` 与客户端、数据表 §3 保持一致。
5. **可 Mock 渐进替换**：每个 Android `MockXxxRepository` 对应明确 API 模块，可逐个切换。

---

## 2. 架构总览

### 2.1 系统上下文

```
                    ┌─────────────────────────────────────────┐
                    │              CampusGo 后端               │
                    │  ┌─────────┐ ┌─────────┐ ┌──────────┐  │
  Android App ─────►│  │ API 网关 │►│ 业务服务 │►│ PostgreSQL│  │
  (Retrofit)        │  │ (可选)   │ │ (单体)  │ │  campusgo │  │
                    │  └─────────┘ └────┬────┘ └──────────┘  │
                    │                   │                     │
                    │              ┌────▼────┐                │
                    │              │  Redis  │                │
                    │              └────┬────┘                │
                    └───────────────────┼─────────────────────┘
                                        │
              ┌─────────────────────────┼─────────────────────────┐
              │                         │                         │
         短信网关                   对象存储 OSS              支付渠道
      (验证码)                  (头像/聊天/送达照)         (充值 Mock→正式)
              │                         │                         │
         推送网关                   OCR 审核                 管理后台 (P1)
      (厂商/FCM)                 (校园卡，P1)
```

### 2.2 MVP 推荐形态：**模块化单体**

一期不拆独立微服务，按 **Maven/Gradle 多模块** 或 **Spring Boot 包分层** 组织代码：

```
campusgo-server/
├── campusgo-api          # REST Controller、DTO、OpenAPI
├── campusgo-application  # 用例编排（Application Service）
├── campusgo-domain       # 实体、枚举、领域服务、Repository 接口
├── campusgo-infrastructure # MyBatis/JPA、Redis、OSS、SMS 适配器
└── campusgo-bootstrap    # Spring Boot 启动、配置
```

**何时拆分微服务**：DAU > 5000、IM/推送独立扩容、或团队 > 5 人专职后端时再考虑 `task-service`、`wallet-service` 独立部署。

### 2.3 请求链路

```
Client
  → HTTPS (TLS 1.2+)
  → Auth Filter (JWT 校验)
  → Rate Limit (Redis)
  → Controller
  → Application Service（事务边界）
  → Domain Service
  → Repository / 外部适配器
  → PostgreSQL / Redis / OSS
```

---

## 3. 技术选型

### 3.1 推荐栈（MVP）


| 类别 | 选型 | 说明 |
| ---- | ---- | ---- |
| 语言 | **Java 17** | 与 Android 团队一致；LTS |
| 框架 | **Spring Boot 3.x** | Web、Security、Validation、Scheduling |
| ORM | **MyBatis-Plus** 或 **Spring Data JPA** | 复杂查询（任务大厅）倾向 MyBatis |
| 数据库 | **PostgreSQL 16+** | 见数据表文档 |
| 缓存 | **Redis 7** | 验证码、限流、抢单辅助锁、会话黑名单 |
| API 文档 | **SpringDoc OpenAPI 3** | 自动生成 Swagger UI |
| 对象存储 | 阿里云 OSS / MinIO（开发） | 图片、校园卡、送达照 |
| 消息（一期） | **HTTP 轮询 + 可选 WebSocket** | IM 一期够用；二期可上 IM SDK |
| 任务调度 | Spring `@Scheduled` | 预约任务生效、超时自动确认 |
| 日志 | SLF4J + Logback | 结构化 JSON（生产） |
| 监控 | Micrometer + Prometheus（可选） | MVP 可先 Actuator health |

### 3.2 备选与约束


| 项 | 决策 |
| -- | ---- |
| Kotlin 后端 | 可选，但团队 Java 统一时优先 Java |
| MySQL | 不采用；已定 PostgreSQL |
| gRPC | MVP 不用；REST 足够 |
| Kafka | MVP 不用；Outbox 模式可 P2 引入 |
| 分布式事务 | 避免；单库本地事务 + 幂等键 |

---

## 4. 服务划分与模块边界

### 4.1 逻辑模块（对应 Android Mock / Navigator）


| 模块 ID | 名称 | 职责 | Android 对应 |
| ------- | ---- | ---- | ------------ |
| **AUTH** | 认证 | 短信验证码、登录注册、Token 刷新、登出 | `LoginActivity`, `SessionManager` |
| **USER** | 用户 | 资料、角色切换、信用分查询、校园卡认证 | `EditProfileActivity`, `VerifyActivity` |
| **ADDR** | 地址 | 常用地址 CRUD、默认地址 | `MockAddressRepository`, T04 |
| **TASK** | 任务 | 草稿、发布、列表、详情、状态流转 | `MockTaskRepository`, T01/T06/M02 |
| **GROUP** | 拼单 | 加入/退出、成员、满员转待抢 | `MockGroupRepository`, T07 |
| **WALLET** | 钱包 | 余额、流水、托管、充值 | `SessionManager`, `MockWalletRepository`, W01 |
| **WITHDRAW** | 提现 | 账户绑定、提现申请、记录 | `MockWithdrawRepository`, W03–W05 |
| **CHAT** | 聊天 | 会话、消息、已读 | `MockChatRepository`, C01 |
| **NOTIFY** | 通知 | 系统通知、已读、深链 payload | `MockMessageRepository`, M03 |
| **TEMPLATE** | 模板 | 系统模板 + 用户模板 | `MockTemplateRepository`, T03 |
| **COMPLAINT** | 投诉 | 提交、记录、状态 | `MockComplaintRepository`, S02/S03 |
| **GROWTH** | 增长 | 积分、签到、商城兑换 | `MockPointsRepository`, G01/G02（P2） |
| **ANALYTICS** | 数据 | 看板、热力图聚合 | `MockDashboardRepository`, D01/L02（P1/P2） |

### 4.2 模块依赖关系

```
AUTH ──► USER
USER ──► ADDR / TEMPLATE / WALLET
TASK ──► WALLET (托管) / NOTIFY / CHAT (接单建会话)
GROUP ──► TASK / WALLET
WITHDRAW ──► WALLET / USER (实名)
COMPLAINT ──► TASK / USER
```

**规则**：`WALLET` 变动只能由 `Application Service` 在事务内调用 `WalletDomainService`，禁止 Controller 直接改余额。

---

## 5. API 设计规范

### 5.1 基础约定


| 项 | 约定 |
| -- | ---- |
| 协议 | HTTPS，`/api/v1/...` |
| 风格 | RESTful 资源 + 动作型子资源（如 `/tasks/{id}/grab`） |
| 认证 | `Authorization: Bearer <access_token>` |
| 内容类型 | `application/json; charset=utf-8` |
| 时间 | ISO-8601，`2026-07-31T08:00:00+08:00` |
| 分页 | `page`（从 1）、`pageSize`（默认 20，最大 50） |
| 排序 | `sort=createdAt,desc` |
| 幂等 | 写操作支持 `Idempotency-Key` 头（充值、抢单、发布） |

### 5.2 统一响应包装

```json
{
  "code": 0,
  "message": "ok",
  "data": { },
  "traceId": "a1b2c3d4"
}
```

**业务码约定**：


| code | 含义 |
| ---- | ---- |
| 0 | 成功 |
| 40001 | 参数校验失败 |
| 40100 | 未登录 / Token 无效 |
| 40101 | Token 过期 |
| 40300 | 无权限（如未校园认证不能抢单） |
| 40400 | 资源不存在 |
| 40900 | 并发冲突（如任务已被抢） |
| 40901 | 余额不足 |
| 40902 | 状态不允许该操作 |
| 42900 | 请求过于频繁 |
| 50000 | 服务器内部错误 |

HTTP 状态码：成功统一 **200**（业务错误也返回 200 + code），仅网关/框架层用 401/429/503。与 Android `ApiResponse<T>` 对齐。

### 5.3 金额字段

**推荐 API 层**：

```json
{
  "rewardCent": 1500,
  "rewardYuan": "15.00"
}
```

服务端以 `rewardCent` 为准；`rewardYuan` 仅展示。Android 现有 `double` 元可在 Mapper 层 `/ 100` 转换。

### 5.4 枚举传输

字符串枚举，与库表、Android 一致，例如 `"status": "PENDING"`，禁止魔法数字。

---

## 6. 认证与会话

### 6.1 登录流程（替代 Android Demo 密码登录）

```
1. POST /api/v1/auth/sms/send     { phone, scene: "LOGIN" }
2. POST /api/v1/auth/login         { phone, code }
3. 返回 { accessToken, refreshToken, expiresIn, userProfile }
4. Android SessionManager 存 Token + 拉取 profile 覆盖本地 Mock 字段
```

**开发环境**：可配置 `campusgo.auth.dev-bypass=true`，固定验证码 `123456`（与当前 Demo 一致），**生产必须关闭**。

### 6.2 Token 策略


| Token | 有效期 | 存储 |
| ----- | ------ | ---- |
| Access Token | 7 天 | 客户端 EncryptedSharedPreferences |
| Refresh Token | 30 天 | 客户端 + 服务端 Redis 白名单/轮换 |

- 40101 → 客户端调 `/auth/refresh`；失败清会话跳登录。
- 登出 → 服务端将 Refresh Token 入黑名单。

### 6.3 鉴权模型

- 默认：**登录即可访问**用户资源。
- 动作级校验在 Domain 层：
  - 抢单：校园认证 `APPROVED` + 信用分 ≥ 400 + 当前角色 `RUNNER`
  - 发布：余额足够托管
  - 提现：已绑定提现账户 + 实名（P1）

---

## 7. 核心领域服务

### 7.1 AUTH 模块


| 方法 | API | 说明 |
| ---- | --- | ---- |
| 发送验证码 | `POST /auth/sms/send` | Redis 限流：同号 60s、同 IP 10/min |
| 登录/注册 | `POST /auth/login` | 新号自动注册 + 初始化 wallet |
| 刷新 Token | `POST /auth/refresh` | |
| 登出 | `POST /auth/logout` | |

### 7.2 USER 模块


| 方法 | API | 说明 |
| ---- | --- | ---- |
| 当前用户 | `GET /users/me` | 昵称、头像、信用分、角色、认证状态 |
| 更新资料 | `PATCH /users/me` | nickname, avatarUrl |
| 切换角色 | `PUT /users/me/role` | `{ "activeRole": "RUNNER" }`，服务端校验门禁 |
| 提交校园认证 | `POST /users/me/campus-auth` | multipart 校园卡图 |
| 认证状态 | `GET /users/me/campus-auth` | |

**初始化**：注册时 `credit_score=500`，创建空 `wallet`。

### 7.3 ADDR 模块


| 方法 | API |
| ---- | --- |
| 列表 | `GET /addresses` |
| 新增 | `POST /addresses` |
| 更新 | `PUT /addresses/{id}` |
| 删除 | `DELETE /addresses/{id}` |
| 设默认 | `PUT /addresses/{id}/default` |

对应 Android `SavedAddress`：`type` ↔ `tag`，`title` ↔ `name`，`detail` ↔ `detail`。

### 7.4 TASK 模块（核心）

#### 7.4.1 草稿


| 方法 | API |
| ---- | --- |
| 列表 | `GET /tasks/drafts` |
| 保存 | `PUT /tasks/drafts/{id}` 或 `POST /tasks/drafts` |
| 删除 | `DELETE /tasks/drafts/{id}` |

草稿存 `task` 表 `status=DRAFT`，或独立 `task_draft` 表（二选一，推荐 **同表 DRAFT** 简化查询）。

#### 7.4.2 发布与列表


| 方法 | API | 说明 |
| ---- | --- | ---- |
| 发布 | `POST /tasks/publish` | 校验 → 托管扣款 → `PENDING`/`GROUPING`/`RESERVING` |
| 任务大厅 | `GET /tasks/hall` | 跑腿员视角，`status in (PENDING, GROUPING)` + 过滤 |
| 我的发布 | `GET /tasks/mine/published` | 含草稿 |
| 我的接单 | `GET /tasks/mine/accepted` | |
| 拼单池 | `GET /tasks/pool` | `mode=GROUP & status=GROUPING` |
| 我的预约 | `GET /tasks/mine/reservations` | |
| 详情 | `GET /tasks/{id}` | 含 runner 摘要、权限 hints |

#### 7.4.3 状态动作（子资源）


| 动作 | API | 状态迁移 |
| ---- | --- | -------- |
| 抢单 | `POST /tasks/{id}/grab` | `PENDING → ACCEPTED` |
| 开始配送 | `POST /tasks/{id}/deliver/start` | `ACCEPTED → DELIVERING` |
| 上传送达照 | `POST /tasks/{id}/deliver/photo` | `DELIVERING → CONFIRMING` |
| 发布者确认 | `POST /tasks/{id}/confirm` | `CONFIRMING → COMPLETED` + 结算 |
| 加价 | `POST /tasks/{id}/raise-price` | 补托管 |
| 转紧急 | `POST /tasks/{id}/emergency` | 模式/加价 |
| 取消 | `POST /tasks/{id}/cancel` | → `CANCELLED` + 退款规则 |
| 评价 | `POST /tasks/{id}/reviews` | `COMPLETED → REVIEWED`（P1） |

每次变更写 `task_status_log`，更新 `task.version`。

### 7.5 GROUP 模块


| 方法 | API | 说明 |
| ---- | --- | ---- |
| 拼单详情 | `GET /tasks/{id}/group` | 成员列表、是否已满 |
| 加入 | `POST /tasks/{id}/group/join` | 选地址 + 分摊扣款 |
| 退出 | `POST /tasks/{id}/group/leave` | 退款 |
| 满员开抢 | 内部 | `GROUPING → PENDING`（自动或 creator 确认） |

### 7.6 WALLET 模块


| 方法 | API | 说明 |
| ---- | --- | ---- |
| 余额 | `GET /wallet` | balanceCent, frozenCent, totalIncomeCent |
| 流水 | `GET /wallet/ledger` | 分页 |
| 充值 | `POST /wallet/topup` | MVP Mock；回调幂等 |
| 托管 | 内部 | `ESCROW_HOLD` / `ESCROW_RELEASE` / `ESCROW_REFUND` |

**账本规则**：任何余额变动必须插入 `wallet_ledger`，同一事务更新 `wallet.balance_cent`。

### 7.7 WITHDRAW 模块


| 方法 | API |
| ---- | --- |
| 绑定账户 | `POST /withdraw/accounts` |
| 解绑 | `DELETE /withdraw/accounts/{id}` |
| 申请提现 | `POST /withdraw/orders` |
| 提现记录 | `GET /withdraw/orders` |

状态：`PENDING → APPROVED → PAID / REJECTED`；拒绝则解冻退回。

### 7.8 CHAT 模块


| 方法 | API | 说明 |
| ---- | --- | ---- |
| 会话列表 | `GET /conversations` | 按角色过滤 |
| 历史消息 | `GET /conversations/{id}/messages` | 游标分页 |
| 发送 | `POST /conversations/{id}/messages` | text/image |
| 已读 | `POST /conversations/{id}/read` | |

接单成功时 **自动创建** `chat_conversation`（publisher ↔ runner，关联 task_id）。

### 7.9 NOTIFY 模块


| 方法 | API |
| ---- | --- |
| 列表 | `GET /notifications` |
| 未读数 | `GET /notifications/unread-count` |
| 标记已读 | `POST /notifications/{id}/read` |
| 全部已读 | `POST /notifications/read-all` |

Payload 需含 Android `NotificationNavigator` 所需字段：`linkType`, `linkTargetId`, 可选 `taskStatus`, `chatPeerName`。

### 7.10 TEMPLATE 模块（P1，可与 MVP 同步）


| 方法 | API |
| ---- | --- |
| 列表 | `GET /templates?scope=system,user` |
| 新建 | `POST /templates` |
| 删除 | `DELETE /templates/{id}` |

系统模板服务端 seed；用户模板按 `user_id` 隔离。

---

## 8. 关键业务流程

### 8.1 发布任务（含托管）

```
PublishActivity 提交
  → POST /tasks/publish
  → 校验：标题、地址、酬劳、模式
  → 计算应付 reward_cent（紧急 +50%）
  → WalletService.hold(userId, amount, taskId)   // balance ↓ frozen/escrow ↑
  → INSERT task (status=PENDING|GROUPING|RESERVING)
  → INSERT task_status_log
  → NotifyService → 附近跑腿员（P1 推送）
  → 返回 taskId, taskNo
  → Android 跳 PublishSuccessActivity
```

**失败回滚**：托管失败则整事务回滚，返回 40901。

### 8.2 抢单（防超抢）

```
POST /tasks/{id}/grab
  → 校验：认证、信用分、角色、任务 status=PENDING、runner_id IS NULL
  → BEGIN
  → SELECT task WHERE id=? FOR UPDATE        -- PostgreSQL 行锁
  → UPDATE task SET runner_id=?, status=ACCEPTED, version=version+1
     WHERE id=? AND status='PENDING' AND runner_id IS NULL
  → IF updated_rows=0 → 40900 已被抢
  → 创建 chat_conversation
  → INSERT task_status_log
  → NotifyService → 发布者「已被接单」
  → COMMIT
```

Redis 可选：`SETNX grab:lock:{taskId}` 5s 防止热点重复提交。

### 8.3 完成与结算

```
跑腿员拍照 → POST .../deliver/photo → CONFIRMING
发布者确认 → POST .../confirm
  → status=COMPLETED
  → WalletService.releaseEscrow(taskId)
       → 发布者 escrow 释放
       → 跑腿者 wallet 入账 INCOME
  → 更新信用分（P1）
  → 通知双方评价
```

### 8.4 拼单加入

```
POST /tasks/{id}/group/join { addressId, payCent }
  → 校验未满员、未重复加入
  → WalletService.hold(joiner, payCent, taskId)
  → INSERT group_member
  → group_joined_count++
  → IF 满员 → task.status=PENDING，通知大厅
```

### 8.5 取消与退款


| 场景 | 规则 |
| ---- | ---- |
| 待接单取消 | 全额退托管给发布者 |
| 已接单取消 | 按 PRD 扣信用 / 部分补偿（Domain 规则表） |
| 拼单退出 | 未开抢可退；已开抢禁止退出 |

### 8.6 配送定位上报

```
POST /tasks/{id}/locations   { lng, lat, recordedAt }
  → INSERT task_location_point
  → 限频：服务端 10–30s 一条
发布者 GET /tasks/{id}/locations/latest 或 WebSocket 推送
```

---

## 9. 数据架构

### 9.1 主库

- **PostgreSQL** 单库 `campusgo`，表结构见 `CampusGo-数据表结构设计.md` §4–§6。
- 连接池：HikariCP，生产建议读写分离 **P2 再做**。

### 9.2 Redis 用途


| Key 模式 | TTL | 用途 |
| -------- | --- | ---- |
| `sms:limit:{phone}` | 60s | 短信频控 |
| `sms:code:{phone}:{scene}` | 5min | 验证码 |
| `token:refresh:{hash}` | 30d | Refresh 白名单 |
| `grab:lock:{taskId}` | 5s | 抢单防抖 |
| `rate:api:{userId}` | 1min | API 限流 |

### 9.3 对象存储


| 路径前缀 | 内容 |
| -------- | ---- |
| `avatar/{userId}/` | 用户头像 |
| `campus/{userId}/` | 校园卡 |
| `delivery/{taskId}/` | 送达照片 |
| `chat/{conversationId}/` | 聊天图片 |

表内只存 HTTPS URL；上传走 **预签名 PUT** 或服务端中转（MVP 可中转简化）。

### 9.4 与 Android 本地存储分工


| 数据 | 服务端 | 客户端 |
| ---- | ------ | ------ |
| Token、引导标记 | — | SharedPreferences |
| 通知开关、缓存大小 | — | SharedPreferences |
| 任务/钱包/聊天 | **权威** | 可选 Room 缓存 |
| 草稿 | **权威**（上线后） | 离线可 Room 队列同步 |

---

## 10. 基础设施与第三方


| 能力 | MVP | 正式 |
| ---- | --- | ---- |
| 短信 | 日志打印 / Mock | 阿里云短信 |
| 支付充值 | Mock 直接加余额 | 微信/支付宝 App 支付 + 回调 |
| 推送 | 不接或仅站内通知 | 厂商通道 + 个推/FCM |
| 地图 | 客户端高德 SDK | 服务端仅存 lng/lat |
| OCR 校园卡 | 人工审核队列 | 阿里云 OCR + 人工复核 |
| 管理后台 | Swagger + SQL | Spring Boot Admin / 自研 P1 |

---

## 11. 安全与合规

### 11.1 应用安全

- 全站 HTTPS；HSTS（生产）。
- JWT 签名 HS256/RS256；密钥环境变量注入。
- 提现账号 AES 加密存库；日志脱敏手机号/学号。
- 上传文件类型白名单、大小限制（如 5MB）。
- 接口限流 + 验证码防刷。

### 11.2 业务安全

- 抢单、支付、提现：**幂等键 + 行锁**。
- 发布者不能接自己的单；拼单不能重复加入。
- 配送位置仅任务发布者可查。

### 11.3 合规

- 用户协议、隐私政策（Android 已有 Mock 页）。
- 账号注销：30 天冷静期后匿名化（PRD）。
- 校园卡照片访问需鉴权 + 短期 signed URL。

---

## 12. 非功能性要求


| 指标 | 目标 | 手段 |
| ---- | ---- | ---- |
| API P95 延迟 | ≤ 500ms（内网） | 索引、避免 N+1、Redis |
| 抢单一致性 | 0 超抢 | 行锁 + 唯一约束 |
| 可用性 | 99.5%（MVP） | 健康检查、单实例可接受 |
| 数据备份 | 日备 | pg_dump + OSS |
| 并发 | 100 QPS（单校 MVP） | 单体 + PG 足够 |

---

## 13. 工程结构与部署

### 13.1 建议仓库布局（Monorepo）

```
CampusGo/
├── android/                    # 已有 Android 工程
├── server/                     # 新建后端
│   ├── campusgo-api/
│   ├── campusgo-application/
│   ├── campusgo-domain/
│   ├── campusgo-infrastructure/
│   ├── campusgo-bootstrap/
│   ├── docker-compose.yml      # PG + Redis + MinIO
│   └── README.md
├── docs/                       # 可选：迁移 *.md
├── CampusGo-*.md
└── 原型图/
```

### 13.2 环境


| 环境 | 用途 | 数据库 |
| ---- | ---- | ------ |
| local | 开发 | Docker PostgreSQL |
| dev | 联调 | 共享 dev 库 |
| staging | 预发 | 独立库 |
| prod | 生产 | 独立库 + 备份 |

### 13.3 Docker Compose（本地）

```yaml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: campusgo
      POSTGRES_USER: campusgo
      POSTGRES_PASSWORD: campusgo
    ports: ["5432:5432"]
  redis:
    image: redis:7
    ports: ["6379:6379"]
  minio:
    image: minio/minio
    command: server /data
    ports: ["9000:9000"]
```

### 13.4 部署（MVP）

- 单台云主机 2C4G：Spring Boot jar + Nginx 反代 + Let's Encrypt。
- 配置：`application-prod.yml` 外置。
- CI：GitHub Actions — `mvn test` → 构建镜像 → 部署（可选）。

---

## 14. 分期交付计划

### 14.1 Phase 0 — 基建（1 周）

- [ ] 初始化 `server/` 工程、Docker Compose
- [ ] Flyway/Liquibase 迁移脚本（P0 表）
- [ ] 统一响应、异常处理、JWT 过滤器
- [ ] SpringDoc OpenAPI 可访问

### 14.2 Phase 1 — MVP 核心（3–4 周）

**目标**：Android 可切真实接口完成主链路。

| 优先级 | 交付 | Android 替换 |
| ------ | ---- | ------------ |
| P0 | Auth + User/me | Login, SessionManager |
| P0 | Address CRUD | MockAddressRepository |
| P0 | Task 发布/列表/详情/抢单/配送/确认/取消 | MockTask*, TaskDetail |
| P0 | Wallet 余额/流水/托管/Mock 充值 | SessionManager 钱包字段 |
| P0 | Notify 列表 | MockMessageRepository |
| P1 | Chat 会话+消息 | MockChatRepository |
| P1 | Draft + Template | MockPublishDraft, MockTemplate |
| P1 | Group join/leave | MockGroupRepository |
| P1 | Withdraw 绑定+申请+记录 | MockWithdrawRepository |

### 14.3 Phase 2 — 增强（2–3 周）

- 校园卡审核、评价、投诉、预约任务调度
- 定位轨迹 API、推送
- 真实支付回调

### 14.4 Phase 3 — 增长与数据

- 积分、签到、商城
- Dashboard、热力图聚合 API
- 管理后台

---

## 15. Android 客户端对接映射

### 15.1 推荐客户端改造顺序

```
1. 新增 data/remote（Retrofit Api + DTO）
2. 新增 domain/repository 接口
3. 实现 data/repository/XxxRepositoryImpl（Remote）
4. CampusGoApp 按 BuildConfig.USE_MOCK 切换 Mock / Remote
5. 逐个 Activity 从 Mock 改为 Repository 注入
```

### 15.2 Mock → API 对照表


| Android Mock | 后端模块 | 首要 API |
| ------------ | -------- | -------- |
| SessionManager.login | AUTH | POST /auth/login |
| SessionManager 钱包字段 | WALLET | GET /wallet |
| MockAddressRepository | ADDR | /addresses |
| MockPublishDraftRepository | TASK | /tasks/drafts |
| MockTemplateRepository | TEMPLATE | /templates |
| MockTaskRepository | TASK | /tasks/hall, /tasks/mine/* |
| MockTaskDetailRepository | TASK | GET /tasks/{id} |
| MockGroupRepository | GROUP | /tasks/{id}/group/* |
| MockWalletRepository | WALLET | GET /wallet/ledger |
| MockWithdrawRepository | WITHDRAW | /withdraw/* |
| MockChatRepository | CHAT | /conversations/* |
| MockMessageRepository | NOTIFY+CHAT | /notifications, /conversations |
| MockComplaintRepository | COMPLAINT | POST /complaints |
| MockPointsRepository | GROWTH | P2 |
| MockDashboardRepository | ANALYTICS | P2 |
| MockHeatmapRepository | ANALYTICS | P2 |

### 15.3 DTO 命名建议

与 Android domain 对齐，减少 Mapper 成本：

| Android | API DTO |
| ------- | ------- |
| TaskListItem | TaskListItemDto |
| TaskDetail | TaskDetailDto |
| SavedAddress | AddressDto |
| PublishDraft | TaskDraftDto |
| GroupOrderDetail | GroupDetailDto |
| WalletTransaction | LedgerItemDto |

---

## 16. 风险与决策记录


| ID | 决策 | 理由 |  revisit |
| -- | ---- | ---- | -------- |
| D-01 | MVP 模块化单体 | 团队小、需求迭代快 | DAU>5000 |
| D-02 | PostgreSQL 非 MySQL | 已有数据表文档、行锁可靠 | — |
| D-03 | REST 非 gRPC | Android Retrofit 成熟 | 内部服务增多时 |
| D-04 | 草稿与 task 同表 DRAFT | 减少表数量 | 草稿字段膨胀时拆表 |
| D-05 | IM 一期 HTTP 轮询 | 降低复杂度 | 消息量/延迟不达标时 |
| D-06 | 充值 MVP Mock | 无支付资质可先联调 | 上线前接真实支付 |
| D-07 | 业务错误 HTTP 200 | 与现有 Android ApiResponse 一致 | 若改 REST 纯语义可调整 |

---

## 17. 附录

### 17.1 MVP API 清单（速查）

```
# Auth
POST   /api/v1/auth/sms/send
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh
POST   /api/v1/auth/logout

# User
GET    /api/v1/users/me
PATCH  /api/v1/users/me
PUT    /api/v1/users/me/role
POST   /api/v1/users/me/campus-auth
GET    /api/v1/users/me/campus-auth

# Address
GET    /api/v1/addresses
POST   /api/v1/addresses
PUT    /api/v1/addresses/{id}
DELETE /api/v1/addresses/{id}
PUT    /api/v1/addresses/{id}/default

# Task
GET    /api/v1/tasks/drafts
POST   /api/v1/tasks/drafts
PUT    /api/v1/tasks/drafts/{id}
DELETE /api/v1/tasks/drafts/{id}
POST   /api/v1/tasks/publish
GET    /api/v1/tasks/hall
GET    /api/v1/tasks/pool
GET    /api/v1/tasks/mine/published
GET    /api/v1/tasks/mine/accepted
GET    /api/v1/tasks/mine/reservations
GET    /api/v1/tasks/{id}
POST   /api/v1/tasks/{id}/grab
POST   /api/v1/tasks/{id}/deliver/start
POST   /api/v1/tasks/{id}/deliver/photo
POST   /api/v1/tasks/{id}/confirm
POST   /api/v1/tasks/{id}/raise-price
POST   /api/v1/tasks/{id}/emergency
POST   /api/v1/tasks/{id}/cancel
POST   /api/v1/tasks/{id}/locations
GET    /api/v1/tasks/{id}/locations/latest

# Group
GET    /api/v1/tasks/{id}/group
POST   /api/v1/tasks/{id}/group/join
POST   /api/v1/tasks/{id}/group/leave

# Wallet
GET    /api/v1/wallet
GET    /api/v1/wallet/ledger
POST   /api/v1/wallet/topup

# Withdraw
GET    /api/v1/withdraw/accounts
POST   /api/v1/withdraw/accounts
DELETE /api/v1/withdraw/accounts/{id}
GET    /api/v1/withdraw/orders
POST   /api/v1/withdraw/orders

# Chat
GET    /api/v1/conversations
GET    /api/v1/conversations/{id}/messages
POST   /api/v1/conversations/{id}/messages
POST   /api/v1/conversations/{id}/read

# Notify
GET    /api/v1/notifications
GET    /api/v1/notifications/unread-count
POST   /api/v1/notifications/{id}/read
POST   /api/v1/notifications/read-all

# Template
GET    /api/v1/templates
POST   /api/v1/templates
DELETE /api/v1/templates/{id}

# Complaint (P1)
POST   /api/v1/complaints
GET    /api/v1/complaints
GET    /api/v1/complaints/{id}
```

### 17.2 修订记录


| 版本 | 日期 | 说明 |
| ---- | ---- | ---- |
| v1.0 | 2026-07-31 | 初稿：架构总览、模块、API、流程、分期、Android 映射 |

---

**下一步建议**：在 `server/` 初始化 Spring Boot 骨架 + Flyway P0 表 + Auth/Task/Wallet 三个模块的 OpenAPI Stub，Android 侧先接 `Auth + GET /users/me + GET /wallet` 验证端到端链路。
