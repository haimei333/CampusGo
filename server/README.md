# CampusGo Server

CampusGo 后端 — **模块化单体** Spring Boot 工程，对应 [`CampusGo-后端架构文档.md`](../CampusGo-后端架构文档.md)。

## 模块结构

```
server/
├── campusgo-domain/          # 枚举、领域模型、Repository 接口
├── campusgo-application/     # 应用服务（用例编排）
├── campusgo-infrastructure/  # JPA、Redis、JWT 适配器
├── campusgo-api/             # REST Controller、Security、OpenAPI
└── campusgo-bootstrap/       # 启动入口、配置、Flyway
```

## 环境要求

- JDK 17+（本机 JDK 21 可用）
- Maven 3.8+
- Docker Desktop（本地 PostgreSQL / Redis）

## 快速启动

### 1. 启动依赖

```bash
cd server
docker compose up -d
```

### 2. 编译 & 运行

**注意：** 这是多模块工程，不要只跑 `campusgo-bootstrap`，需先在 `server/` 根目录编译全部模块：

```bash
mvn clean package -DskipTests
java -jar campusgo-bootstrap/target/campusgo-bootstrap-0.1.0-SNAPSHOT.jar
```

或一条命令（在 `server/` 目录）：

```bash
mvn clean package -DskipTests && java -jar campusgo-bootstrap/target/campusgo-bootstrap-0.1.0-SNAPSHOT.jar
```

若出现 `Could not find artifact com.campusgo:campusgo-api`，说明只编译了 bootstrap，没有先编译依赖模块，按上面命令从根目录执行即可。

默认 profile：`local`（连接 `localhost:5433` / `6379`）。

> **端口说明：** 若本机已安装 PostgreSQL 并占用 5432，CampusGo Docker 使用 **5433** 映射，Navicat 请连 `5433`，不要连 `5432`（那是本机 `ipm_db` 等其它库）。

### 3. 验证

| 地址 | 说明 |
|------|------|
| http://localhost:8080/api/v1/health | 健康检查 |
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/actuator/health | Actuator |

### 4. 登录联调（须先注册）

```bash
# 注册（新手机号）
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"phone\":\"13700137000\",\"password\":\"123456\"}"

# 登录（已注册账号；演示号 13800138000 密码 123456）
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"phone\":\"13800138000\",\"password\":\"123456\"}"

# 带 Token 查资料 / 钱包
curl http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer <accessToken>"

curl http://localhost:8080/api/v1/wallet \
  -H "Authorization: Bearer <accessToken>"
```

## 已实现 API（骨架）

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `GET  /api/v1/users/me`
- `PATCH /api/v1/users/me`
- `PUT  /api/v1/users/me/role`
- `GET  /api/v1/wallet`
- `GET  /api/v1/health`

## Task 模块 Mock API（Android 联调）

- **OpenAPI 契约：** [`openapi/task-api.yaml`](openapi/task-api.yaml)
- **联调说明：** [`docs/task-android-integration.md`](docs/task-android-integration.md)
- **Mock 开关：** `campusgo.task.mock=true`（默认开启，内存数据）
- **Swagger 分组：** Task (Mock)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | `/api/v1/tasks/drafts` | 草稿 CRUD |
| POST | `/api/v1/tasks/publish` | 发布 |
| GET | `/api/v1/tasks/hall` 等 | 列表 |
| GET | `/api/v1/tasks/{id}` | 详情 |
| POST | `/api/v1/tasks/{id}/grab` 等 | 状态动作 |

## Task 模块 Mock API（Android 联调）

- **OpenAPI 契约：** [`openapi/task-api.yaml`](openapi/task-api.yaml)
- **联调说明：** [`docs/task-android-integration.md`](docs/task-android-integration.md)
- **开关：** `campusgo.task.mock=true`（默认开启，内存 Mock；关闭后待接真实 DB）

Swagger 分组 **Task (Mock)** 含 18 个端点：草稿、发布、大厅/我的列表、详情、抢单、配送、确认、加价、取消等。

## 测试

```bash
mvn test
```

`test` profile 使用 H2 内存库，无需 Docker。

## 任务持久化（Phase 2）

- Flyway：`V2__task_core.sql`（`task` / `task_status_log` / `task_group_member` / `wallet_ledger`）
- local 默认：`campusgo.task.mock=false` → `TaskController` + PostgreSQL
- Mock 回退：`campusgo.task.mock=true` → `TaskMockController` 内存数据
- 首次启动空表时 `TaskDataSeeder` 写入演示任务；流水写入 `wallet_ledger`

## 下一步

1. ~~发布托管与确认结算对账完善~~（已实现：`hold` / `releaseEscrow` / `refundEscrow` + 流水）
2. 聊天 / 投诉 / 积分商城 API 化

## 资金托管与结算

| 动作 | 余额 | 冻结 | 流水 |
|------|------|------|------|
| 发布 / 拼单支付 / 加价 | ↓ | ↑ | `ESCROW_HOLD` / `RAISE` |
| 确认完成 | — | ↓（发布者） | `ESCROW_RELEASE` + 跑腿员 `INCOME` |
| 取消 / 退出拼单 | ↑ | ↓ | `ESCROW_REFUND` |

拼单：发起人仅托管本人份额，成员加入时追加托管；满员后 `escrowCent` 合计约等于总酬劳。

## 地址簿（Phase 3 部分）

- Flyway：`V4__user_address.sql`
- API：`GET/POST/PUT/DELETE /api/v1/addresses`
- 演示账号 `13800138000` 首次启动自动种子地址

## 预约占位（Phase 3 部分）

- Flyway：`V5__task_reserve_slot.sql`
- API：`POST /api/v1/tasks/{id}/reserve/hold|release|confirm`
- 任务大厅展示 `RESERVE` 预约任务；「我的预约」Tab 区分发布者 / 跑腿员占位
- 演示：13900139000 发布预约任务，13800138000 跑腿员可占位
