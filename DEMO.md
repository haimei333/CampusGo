# CampusGo 联调演示指南（路线 B）

前后端联调演示：Android `USE_REMOTE_API=true` + Spring Boot 后端（**local 默认任务持久化到 PostgreSQL**）。

---

## 1. 环境要求


| 组件             | 版本 / 说明                 |
| -------------- | ----------------------- |
| JDK            | 17+                     |
| Maven          | 3.8+（在 `server/` 根目录构建） |
| Docker Desktop | PostgreSQL + Redis      |
| Android Studio | 模拟器 API 26+，或真机         |


---

## 2. 启动后端

**重要：** Docker Desktop 里看到 Images 不够，必须让 **Containers 运行起来**。

```bash
cd server
docker compose up -d          # 启动 postgres / redis（必须 Up）
mvn clean package -DskipTests
java -jar campusgo-bootstrap/target/campusgo-bootstrap-0.1.0-SNAPSHOT.jar
```

保持运行 `java -jar` 的终端窗口**不要关闭**。

验证（PowerShell）：

```powershell
curl.exe http://localhost:8080/api/v1/health
# 应返回 {"code":0,"message":"ok","data":{"status":"UP",...}}
```

- Swagger：[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

> PostgreSQL 映射端口 **5433**（非 5432），数据库 `campusgo` / 用户密码均为 `campusgo`。

---

## 3. 启动 Android

配置在 `FeatureFlags.java`：

- `API_BASE_URL = "http://10.0.2.2:8080/"`（模拟器访问本机）
- `USE_REMOTE_API = true`

**真机联调**：将 `API_BASE_URL` 改为电脑局域网 IP，例如 `http://192.168.1.100:8080/`，并确保手机与电脑同一 WiFi。

在 Android Studio 运行 `app` Debug 包即可。

---

## 4. 演示脚本（约 5 分钟）

### 4.1 注册 / 登录

1. 打开 App → 登录页
2. **未注册手机号**点「没有账号？去注册」→ 填写手机号、密码、确认密码 → 注册并登录
3. **已有账号**（如演示号 `13800138000` / 密码 `123456`）直接登录
4. 未注册账号直接登录会提示「账号不存在，请先注册」

> 登录不再自动建号；须先走注册接口。

### 4.2 浏览任务大厅（跑腿员身份）

1. 底部切到 **任务** Tab
2. 若当前是发布者身份，先在 **首页** 或 **我的** 切到跑腿员（需先完成校园认证，见下方说明）
3. 在 **任务大厅** 浏览可抢单任务（数字 ID，Navicat `task` 表可见）
4. **未满员拼单**在 **拼单池**，点进去是拼单详情，不是抢单
5. 点击大厅任务卡片 → 进入详情 → 可抢单

> 任务已持久化到 PostgreSQL（`campusgo.task.mock=false`）。重启后端后数据仍在。

### 4.3 抢单

1. 在任务详情页点击 **抢单**
2. 成功后状态变为「已接单」，可继续 **开始配送 → 确认送达 → 确认收货**

> **校园认证（联调模式）**：切换跑腿员时，**后端**会检查 `campus_status = APPROVED`。请走：**我的 → 校园认证 → 填写姓名/学号 → 提交**（会调用 `POST /api/v1/users/me/campus-auth`，演示环境提交即通过）。若之前只在本地 Mock 认证过，请**重新提交一次**或**退出重新登录**以同步服务端状态。

### 4.4 发布任务（发布者身份）

1. 切回 **发布者** 身份（首页顶部切换）
2. 点击底部 **发布** FAB
3. 完成三步向导：类型 → 填写地址/酬劳 → 确认发布
4. 发布成功 → **查看任务** 或回 **任务 → 我的发布** 查看

### 4.5 钱包与资料

- **我的 → 我的钱包**：余额从 `GET /api/v1/wallet` 同步（新用户默认 ¥128.50）
- **我的** 页：昵称、信用分从 `GET /api/v1/users/me` 同步

---

## 5. 已接入 API 的页面


| 页面                            | API                                     |
| ----------------------------- | --------------------------------------- |
| 登录                            | `POST /api/v1/auth/login`               |
| 任务大厅 / 拼单池 / 我的发布 / 我的接单      | `GET /api/v1/tasks/`*                   |
| 任务详情 / 抢单 / 配送 / 确认 / 加价 / 取消 | `POST/GET /api/v1/tasks/{id}/`*         |
| 发布任务                          | `POST /api/v1/tasks/publish`            |
| 我的资料                          | `GET /api/v1/users/me`                  |
| 钱包 / 充值 / 流水                  | `GET/POST /api/v1/wallet`*              |
| 通知列表 / 已读                     | `GET/POST /api/v1/notifications`*       |
| 提交评价                            | `POST /api/v1/tasks/{id}/reviews`       |
| 拼单详情 / 加入 / 退出                | `GET/POST /api/v1/tasks/{id}/group/`*   |
| 首页推荐（跑腿员）                     | `GET /api/v1/tasks/hall`                |
| 常用地址                          | `GET/POST/PUT/DELETE /api/v1/addresses` |
| 预约占位 / 取消 / 确认接单              | `POST /api/v1/tasks/{id}/reserve/`*     |


仍走本地 Mock 的模块：积分商城、提现流水详情、投诉、热力图、看板。

### 4.6 钱包闭环 → 通知 → 评价（联调脚本）

1. **发布者**发布任务（步骤 4.4）→ 钱包可用余额减少（托管），流水出现「任务托管」
2. **跑腿员**抢单 → 完成配送 → 上传送达照 → **发布者**点「确认收货」
3. 确认后后端自动：`releaseEscrow`（发布者）+ `creditIncome`（跑腿员）+ 写入通知
4. 双方打开 **消息 → 通知** Tab：
   - 跑腿员可见「订单已完成，酬劳已到账」
   - 双方可见「请评价本次任务」→ 点击进入评价页
5. 提交评价后信用分更新（`GET /api/v1/users/me` 可验证）
6. **我的 → 我的钱包** 可看到「任务收入 / 托管释放」流水

---

## 6. 种子任务（PostgreSQL，首次启动自动写入）

任务 ID 为数字字符串（如 `1`、`2`），可在 Navicat 的 `task` 表查看。


| 类型   | 说明                       |
| ---- | ------------------------ |
| 大厅   | 普通待抢 / 紧急 / 拼单满员待抢       |
| 拼单池  | 未满员拼单（演示账号可加入）           |
| 我的发布 | 登录用户 `13800138000` 名下待接单 |


> 切回内存 Mock：在 `application-local.yml` 设 `campusgo.task.mock: true` 后重启。

---

## 7. 常见问题

**登录报「网络连接失败」**

1. Docker Desktop 是否 **Running**？
2. 运行 `docker compose ps`，`campusgo-postgres`、`campusgo-redis` 是否为 **Up**？
3. 运行 `curl.exe http://localhost:8080/api/v1/health` 是否有 JSON 响应？
4. `java -jar` 后端进程是否在运行？（关掉终端 = 关掉后端）
5. **模拟器**用 `10.0.2.2`；**真机**改 `FeatureFlags.API_BASE_URL` 为电脑局域网 IP

**后端启动报 Flyway schema history**

已在 `application-local.yml` 配置 `baseline-on-migrate: true`。若仍失败，重新 `mvn package` 后再启动 jar。

**任务列表为空**

- 确认 `campusgo.task.mock=true`（默认开启）
- 查看 Logcat 中 OkHttp 日志

**抢单提示需认证**

- 我的 → 校园认证 → 完成本地认证流程

**后端启动失败（数据库）**

- 确认 Docker 中 Postgres 在 **5433** 端口运行
- `docker compose ps` 检查容器状态

---

## 8. 切换离线演示

将 `android/app/build.gradle` 中 `USE_REMOTE_API` 改为 `false`，重新编译即可使用纯本地 Mock 数据，无需后端。

或修改 `com.campusgo.core.config.FeatureFlags` 中的 `USE_REMOTE_API` / `API_BASE_URL`。