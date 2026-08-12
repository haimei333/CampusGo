# Task 模块 API — Android 联调说明

> OpenAPI 契约：[`task-api.yaml`](../openapi/task-api.yaml)  
> Mock 实现：`TaskMockController` + `TaskMockService`（内存数据，默认开启）  
> 关闭 Mock：`campusgo.task.mock=false`（后续接真实 TaskService 时使用）

---

## 1. 启动

```bash
cd server
docker compose up -d
mvn clean package -DskipTests
java -jar campusgo-bootstrap/target/campusgo-bootstrap-0.1.0-SNAPSHOT.jar
```

- Swagger UI：http://localhost:8080/swagger-ui.html（分组 **Task (Mock)**）
- 除 `GET /tasks/hall` 等少数接口外，均需 `Authorization: Bearer <token>`

---

## 2. 接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/tasks/drafts` | 草稿列表 |
| POST | `/api/v1/tasks/drafts` | 新建草稿 |
| PUT | `/api/v1/tasks/drafts/{id}` | 更新草稿 |
| DELETE | `/api/v1/tasks/drafts/{id}` | 删除草稿 |
| POST | `/api/v1/tasks/publish` | 发布任务 |
| GET | `/api/v1/tasks/hall` | 任务大厅 |
| GET | `/api/v1/tasks/pool` | 拼单池 |
| GET | `/api/v1/tasks/mine/published` | 我的发布 |
| GET | `/api/v1/tasks/mine/accepted` | 我的接单 |
| GET | `/api/v1/tasks/mine/reservations` | 我的预约 |
| GET | `/api/v1/tasks/{id}` | 任务详情 |
| POST | `/api/v1/tasks/{id}/grab` | 抢单 |
| POST | `/api/v1/tasks/{id}/deliver/start` | 开始配送 |
| POST | `/api/v1/tasks/{id}/deliver/photo` | 上传送达照 |
| POST | `/api/v1/tasks/{id}/confirm` | 发布者确认 |
| POST | `/api/v1/tasks/{id}/raise-price` | 加价 |
| POST | `/api/v1/tasks/{id}/emergency` | 转紧急 |
| POST | `/api/v1/tasks/{id}/cancel` | 取消 |

---

## 3. Android DTO 映射

| Android | API JSON 字段 |
|---------|---------------|
| `TaskListItem.id` | `id` |
| `TaskListItem.title` | `title` |
| `TaskListItem.statusLabel` | `statusLabel` |
| `TaskListItem.description` | `description` |
| `TaskListItem.priceLabel` | 用 `priceYuan` 格式化为 `¥xx.xx` |
| `TaskListItem.category/mode/status` | 同名字符串枚举 |
| `TaskListItem.navTarget` | `navTarget`：`T01`/`T06`/`T07` |
| `TaskDetail.reward` | `rewardCent / 100.0` |
| `TaskDetail.orderNo` | `orderNo` |
| `TaskDetail.runner*` | `runner` 对象 |
| `PublishDraft` | `TaskDraftDto` |

**金额约定：** 请求/响应以 `*Cent`（分）为准；展示用 `*Yuan` 字符串。

---

## 4. 联调示例

```bash
# 1. 登录
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","password":"123456"}' | jq -r '.data.accessToken')

# 2. 任务大厅
curl -s "http://localhost:8080/api/v1/tasks/hall?page=1&pageSize=20" \
  -H "Authorization: Bearer $TOKEN"

# 3. 抢单
curl -s -X POST "http://localhost:8080/api/v1/tasks/h1/grab" \
  -H "Authorization: Bearer $TOKEN"

# 4. 发布
curl -s -X POST http://localhost:8080/api/v1/tasks/publish \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title":"代取快递",
    "mode":"NORMAL",
    "category":"EXPRESS",
    "pickupAddress":"菜鸟驿站",
    "deliveryAddress":"6号楼",
    "timeLabel":"今天",
    "rewardCent":800
  }'
```

---

## 5. Mock 种子数据 ID

| ID | 说明 |
|----|------|
| `h1` | 大厅 · 普通待抢 |
| `h2` | 大厅 · 拼单中 → nav T07 |
| `h3` | 大厅 · 紧急 |
| `p1` | 草稿 |
| `p2` | 我的发布 · 待接单 |
| `t1` | 我的接单 · 配送中 |
| `pool1` | 拼单池 |

---

## 6. Android Retrofit 建议

```java
// baseUrl: http://10.0.2.2:8080/  (模拟器) 或 http://<电脑局域网IP>:8080/
public interface TaskApi {
    @GET("api/v1/tasks/hall")
    Call<ApiResponse<PageResponse<TaskListItemDto>>> hall(
            @Query("page") int page, @Query("pageSize") int pageSize);

    @GET("api/v1/tasks/{id}")
    Call<ApiResponse<TaskDetailDto>> detail(@Path("id") String id);

    @POST("api/v1/tasks/publish")
    Call<ApiResponse<PublishTaskResponse>> publish(@Body PublishTaskRequest body);
}
```

Mock 阶段可先 `USE_MOCK=false` 只切 Task 模块，Auth 仍走本地或一并接 API。
