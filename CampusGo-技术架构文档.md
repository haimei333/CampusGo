# CampusGo Android 技术架构文档

> **版本**: v1.1  
> **日期**: 2026-07-27  
> **状态**: 初稿  
> **客户端语言**: Java  
> **服务端数据库**: PostgreSQL  
> **依据文档**:
>
> - `CampusGo-PRD.md` v1.0
> - `CampusGo-界面拆分.md` v1.1
> - `CampusGo-功能拆解与交互规格.md`
> - `CampusGo-SoftUI-DesignToken.md`
> - `CampusGo-数据表结构设计.md`（**服务端库：PostgreSQL**）
> - `原型图/` HTML 交互原型（走查参考，非实现规范）

---

## 目录

1. [文档目标与范围](#1-文档目标与范围)
2. [架构总览](#2-架构总览)
3. [技术选型](#3-技术选型)
4. [工程与包结构](#4-工程与包结构)
5. [分层职责](#5-分层职责)
6. [领域模型与状态机](#6-领域模型与状态机)
7. [UI 与导航架构](#7-ui-与导航架构)
8. [网络与数据架构](#8-网络与数据架构)
9. [关键子系统设计](#9-关键子系统设计)
10. [安全与隐私](#10-安全与隐私)
11. [非功能指标落地](#11-非功能指标落地)
12. [分期交付与里程碑](#12-分期交付与里程碑)
13. [质量保障](#13-质量保障)
14. [风险与决策记录](#14-风险与决策记录)
15. [附录](#15-附录)

---

## 1. 文档目标与范围

### 1.1 目标

本文定义 CampusGo **Android 客户端**的技术架构，指导工程搭建、模块划分、核心链路实现与分期交付，使开发、联调、评审有统一依据。

### 1.2 范围


| 在范围内                     | 不在范围内（可另立文档）                 |
| ------------------------ | ---------------------------- |
| Android App 架构与技术选型      | 服务端详细库表设计（见数据表文档，**PostgreSQL**） |
| 客户端分层、包结构、导航             | 运营后台完整设计                     |
| 领域模型与任务状态机               | iOS / 小程序                    |
| 关键能力（地图、定位、支付、IM）客户端接入方式 | 具体接口字段的最终版 OpenAPI（一期可 Mock） |


### 1.3 权威冲突处理


| 冲突类型                   | 优先采用                             |
| ---------------------- | -------------------------------- |
| 页面增减 / Tab 角色规则 / 通知深链 | `CampusGo-界面拆分.md`               |
| 业务规则 / 信用分 / 状态机语义     | `CampusGo-PRD.md`                |
| 视觉色值 / 圆角 / 组件风格       | `CampusGo-SoftUI-DesignToken.md` |
| HTML 原型与文档不一致          | 以文档为准；原型仅作布局与文案参考                |
| 原型内「角色×状态演示条」          | **正式 App 不实现**                   |


---

## 2. 架构总览

### 2.1 系统上下文

```
┌─────────────┐     HTTPS/JWT      ┌──────────────────┐
│  CampusGo   │ ◄───────────────► │  业务 API 服务    │
│  Android    │                   │  (任务/钱包/用户)  │
│  App        │ ◄── WebSocket ──► │  消息服务（可选）  │
│  (Java)     │ ◄── 推送通道 ────► │  推送网关         │
└──────┬──────┘                   └────────┬─────────┘
       │                                   │
       ├─ 高德地图 SDK                     ├─ 支付渠道（托管）
       ├─ CameraX                          ├─ 对象存储（图片）
       └─ 前台定位服务                     └─ OCR / 审核（校园卡）
```

### 2.2 客户端架构风格

采用 **单 Activity + 多 Fragment + MVVM** 的分层架构：

```
┌──────────────────────────────────────────────┐
│                 Presentation                 │
│   Activity / Fragment / Dialog / Adapter     │
│   ViewModel + LiveData（UiState）            │
├──────────────────────────────────────────────┤
│                   Domain                     │
│   Model / UseCase / Repository Interface     │
├──────────────────────────────────────────────┤
│                    Data                      │
│   Remote(API) / Local(Room·Prefs) / Mapper   │
└──────────────────────────────────────────────┘
```

**设计原则**：

1. **一页一主任务**：与界面拆分原则一致。
2. **状态驱动详情**：履约操作收敛在 `TaskDetail`（T06），少开平行页。
3. **角色视角分离**：首页 / 任务 Tab 随 `UserRole` 切换。
4. **主流程优先**：P0 先行，P1/P2 可插拔扩展。
5. **服务端权威**：抢单、结算、状态流转以服务端结果为准，客户端乐观更新需可回滚。

### 2.3 核心产品轴心

整个客户端围绕两条轴心展开：


| 轴心                | 说明                               |
| ----------------- | -------------------------------- |
| **任务状态机**         | 全 App 唯一；详情页、列表角标、通知深链共用         |
| **双身份（UserRole）** | 发布者 / 跑腿员；驱动 M01 内容、M02 Tab、门禁校验 |


---

## 3. 技术选型

### 3.1 基础环境


| 项                      | 选型                                            | 说明                                                        |
| ---------------------- | --------------------------------------------- | --------------------------------------------------------- |
| 语言                     | **Java 11+**                                  | 团队选择 Java；本机可用 JDK 17/21，Gradle JDK 以 Android Studio 配置为准 |
| minSdk                 | **26**（Android 8.0）                           | 对齐 PRD §8.3                                               |
| targetSdk / compileSdk | **34**（Android 14）                            | 对齐 PRD                                                    |
| 构建                     | Gradle + Android Gradle Plugin                | 推荐使用 Android Studio 默认稳定组合                                |
| UI                     | **XML + ViewBinding + Material Components 3** | Java 项目首选；不采用 Compose（生态偏 Kotlin）                         |


### 3.2 框架与库


| 类别    | 选型                                        | 用途                   |
| ----- | ----------------------------------------- | -------------------- |
| 架构组件  | ViewModel、LiveData、Lifecycle              | MVVM                 |
| 导航    | Navigation Component                      | 单 Activity 路由、深链     |
| 网络    | Retrofit 2 + OkHttp 4                     | REST                 |
| JSON  | Gson                                      | DTO 序列化              |
| 本地数据库 | Room                                      | 草稿、会话缓存（可选）          |
| 键值存储  | SharedPreferences 或 DataStore Preferences | Token、引导标记、当前角色      |
| 图片    | Glide                                     | 头像、聊天图、送达照           |
| 异步    | 主线程 LiveData + 后台 Executor / 可选 RxJava 3  | 不强制引入协程              |
| DI    | 一期可手动单例；规模上来后可选 Hilt                      | 降低初期复杂度              |
| 地图    | 高德 Android SDK                            | T05 选点、L01 追踪、L02 热力 |
| 定位    | 系统定位 + 自研 Foreground Service              | 配送中上报                |
| 相机    | CameraX                                   | T08 实时拍照             |
| 推送    | 厂商通道 / FCM（按部署环境选定）                       | 订单与系统通知              |


### 3.3 明确不采用（一期）


| 项                   | 原因                           |
| ------------------- | ---------------------------- |
| Jetpack Compose     | Java 体验与资料不如 XML             |
| WebView 直接套 HTML 原型 | 无法满足定位前台服务、CameraX、原生体验与性能要求 |
| 客户端权威抢单             | 必须服务端行锁，防超抢                  |


---

## 4. 工程与包结构

### 4.1 建议工程形态

```
CampusGo/
├── docs/                          # 可选：文档迁入
├── 原型图/                         # 设计参考（不参与编译）
├── CampusGo-*.md                  # 产品与架构文档
└── android/                       # Android 工程根目录（建议）
    ├── settings.gradle
    ├── build.gradle
    └── app/
        ├── build.gradle
        └── src/main/
            ├── AndroidManifest.xml
            ├── java/com/campusgo/
            └── res/
```

> 说明：当前仓库以产品文档与原型为主；Android 工程可建在 `android/` 子目录，避免与文档混杂。

### 4.2 应用包结构

```
com.campusgo
├── CampusGoApp                    // Application
├── ui
│   ├── main                       // MainActivity、BottomNav、4 Tab
│   ├── auth                       // A01 Login、A02 Guide、A03 EditProfile
│   ├── home                       // 发布者/跑腿员首页
│   ├── tasks                      // M02 任务 Tab（大厅/发布/接单/拼单池/预约）
│   ├── task
│   │   ├── publish                // T01、T02
│   │   ├── detail                 // T06 ★
│   │   ├── group                  // T07
│   │   ├── photo                  // T08
│   │   ├── review                 // T10
│   │   └── sheet                  // T11 取消、加价、选券
│   ├── message                    // M03、C01
│   ├── map                        // T05、L01、L02
│   ├── wallet                     // W01–W05
│   ├── profile                    // M04、S01–S06、D01
│   └── growth                     // G01、G02
├── domain
│   ├── model
│   ├── repository                 // 接口
│   └── usecase
├── data
│   ├── remote                     // ApiService、DTO、Interceptor
│   ├── local                      // Room、Prefs
│   ├── mapper
│   └── repository                 // 实现
└── core
    ├── network                    // RetrofitClient、ApiResult、ErrorCode
    ├── design                     // 可复用 View / BindingAdapter（可选）
    ├── navigation                 // DeepLink、Route 常量
    ├── session                    // SessionManager（Token、User、Role）
    └── location                   // DeliveryLocationService
```

### 4.3 资源组织

```
res/
├── values/
│   ├── colors.xml                 // 对齐 Soft UI Design Token
│   ├── dimens.xml
│   ├── strings.xml
│   └── themes.xml
├── layout/                        // 按页面命名：fragment_task_detail.xml
├── menu/
├── navigation/
│   ├── nav_main.xml
│   ├── nav_auth.xml
│   └── nav_task.xml               // 或单一大图，按团队习惯拆分
└── drawable/
```

---

## 5. 分层职责

### 5.1 Presentation（ui）


| 职责  | 说明                                         |
| --- | ------------------------------------------ |
| 渲染  | XML 布局 + ViewBinding                       |
| 观察  | 观察 ViewModel 的 `LiveData<UiState>`         |
| 交互  | 点击事件转为 ViewModel 意图（intent / method）       |
| 导航  | 通过 Navigation 跳转；不在 Fragment 里直接调 Retrofit |


**约束**：Fragment 不持有业务规则判断（如信用分门禁逻辑应在 ViewModel / UseCase）。

### 5.2 Domain


| 职责   | 说明                                                         |
| ---- | ---------------------------------------------------------- |
| 模型   | `Task`、`User`、`Wallet` 等与 UI/网络无关的领域对象                     |
| 用例   | `GrabTaskUseCase`、`PublishTaskUseCase`、`SwitchRoleUseCase` |
| 仓库接口 | `TaskRepository` 等，便于 Mock 与单测                             |


### 5.3 Data


| 职责     | 说明                    |
| ------ | --------------------- |
| Remote | Retrofit 接口、DTO       |
| Local  | Room 草稿、消息缓存；Prefs 会话 |
| Mapper | DTO ↔ Domain          |
| 实现仓库   | 协调远程与本地，处理缓存策略        |


### 5.4 依赖方向

```
ui → domain ← data
ui 不得直接依赖 data.remote.ApiService（应经 Repository）
```

---

## 6. 领域模型与状态机

### 6.1 用户与身份

```java
public enum UserRole {
    PUBLISHER,
    RUNNER
}

public class User {
    String userId;
    String nickname;
    String avatarUrl;
    String phoneMasked;
    int creditScore;          // 0–1000，初始 500
    boolean campusVerified;
    UserRole activeRole;      // 当前顶栏身份
}
```

**切跑腿员门禁（`SwitchRoleUseCase`）**：

```
目标 = RUNNER
  ├─ 未校园卡认证 → NeedVerify → S01
  ├─ creditScore < 400 → CreditBlocked → S06
  └─ 否则切换成功 → 刷新 M01 / M02 默认 Tab / M04 看板维度
```

进行中任务不因身份切换而中断。

### 6.2 任务模式与分类

```java
public enum TaskMode {
    NORMAL,      // 普通
    GROUP,       // 拼单
    EMERGENCY,   // 紧急
    RESERVE      // 预约
}

public enum ServiceCategory {
    EXPRESS,     // 代取快递
    BUY,         // 代买物品
    ERRAND       // 代办事务
}
```

T01 发布页须将 **模式** 与 **服务分类** 分栏，不得混为一组 Chip。

### 6.3 任务状态机（全平台唯一）

```java
public enum TaskStatus {
    DRAFT,        // 草稿
    GROUPING,     // 拼单中
    RESERVING,    // 预约中
    PENDING,      // 待接单
    ACCEPTED,     // 已接单
    DELIVERING,   // 配送中
    CONFIRMING,   // 待确认
    COMPLETED,    // 已完成
    REVIEWED,     // 已评价（双方评完或超时默认好评后）
    CANCELLED     // 已取消
}
```

**主路径流转**：

```
DRAFT → PENDING（普通/紧急）
     → GROUPING →（满员）PENDING
     → RESERVING →（到点/确认）PENDING 或 ACCEPTED
PENDING → ACCEPTED → DELIVERING → CONFIRMING → COMPLETED → REVIEWED
任意可取消节点 → CANCELLED（规则见界面拆分）
```

### 6.4 任务详情 UI 决策（T06）

`TaskDetailViewModel` 根据 `status + isPublisher + isAssignedRunner` 生成 `TaskDetailUiState`：


| 状态         | 发布者底栏                | 跑腿员底栏           |
| ---------- | -------------------- | --------------- |
| PENDING    | 加价、转紧急、取消、分享         | 抢单              |
| ACCEPTED   | 联系、取消（规则内）           | 开始配送、联系、取消（规则内） |
| DELIVERING | 位置追踪、联系、投诉（**不可取消**） | 我已送达→T08、联系     |
| CONFIRMING | 确认完成、有异议→S02         | 等待；5 分钟内可补拍     |
| COMPLETED  | 去评价、看结算              | 去评价、看结算         |


### 6.5 信用分规则（客户端展示与拦截）


| 项         | 值              |
| --------- | -------------- |
| 范围        | 0–1000         |
| 初始        | 500            |
| ≥ 600     | 可接全部类型         |
| 400–600   | 仅普通任务          |
| < 400     | 暂停接单；引导 S06    |
| 发布者 < 400 | 发布需额外押金（服务端校验） |


---

## 7. UI 与导航架构

### 7.1 Activity 划分


| 组件                         | 职责                         |
| -------------------------- | -------------------------- |
| `SplashActivity`（可选）       | Token 校验，分流 Login / Main   |
| `LoginActivity`            | A01，**无 BottomNavigation** |
| `GuideActivity` 或 Fragment | A02 四步引导（P2，可后置）           |
| `MainActivity`             | 唯一主壳：4 Tab + FAB           |


### 7.2 主壳结构

```
MainActivity
├── BottomNavigationView
│   ├── HomeFragment          // M01
│   ├── TasksFragment         // M02
│   ├── MessageFragment       // M03
│   └── ProfileFragment       // M04
└── FAB「发布任务」→ PublishFragment（双身份可见）
```

### 7.3 M02 Tab 裁剪规则


| Tab  | 发布者     | 跑腿员  | 卡片去向                        |
| ---- | ------- | ---- | --------------------------- |
| 任务大厅 | 隐藏或次级   | 默认   | 普通/紧急→T06；未满拼单→T07；满员待抢→T06 |
| 拼单池  | 展示      | 可展示  | T07                         |
| 我的发布 | 默认（含草稿） | 隐藏   | 草稿→T01；拼单中→T07；其余→T06       |
| 我的接单 | 隐藏      | 展示   | T06                         |
| 我的预约 | 我发起的    | 我占位的 | T06                         |


实现建议：`TasksFragment` 内根据 `SessionManager.getActiveRole()` 动态配置 `TabLayout` + `ViewPager2`。

### 7.4 导航图建议

```
nav_auth:  Login → (Guide) → Main
nav_main:  四个 Tab（各自可有 nested graph）
二级页:    task_detail/{taskId}
           publish
           group_detail/{groupId}
           photo_confirm/{taskId}
           wallet / topup / withdraw ...
```

### 7.5 通知深链路由

统一由 `NotificationRouter` 解析 payload → Navigation Deep Link：


| 通知语义    | 目标                |
| ------- | ----------------- |
| 任务已被接单  | T06               |
| 请确认收货   | T06（CONFIRMING）   |
| 请评价     | T10               |
| 新消息     | C01               |
| 请确认预约接单 | T06               |
| 请提交申诉   | S02 `mode=appeal` |
| 仲裁结果    | S03               |
| 提现结果    | W03               |
| 认证结果    | S01               |
| 紧急需人工   | S06               |


建议 Scheme：`campusgo://task/{id}?statusHint=confirming`

### 7.6 设计系统落地

将 Design Token 映射到 `colors.xml` / `themes.xml`：


| Token        | 资源名示例             | Hex       |
| ------------ | ----------------- | --------- |
| brand        | `cg_brand`        | `#3D9B8F` |
| brand_soft   | `cg_brand_soft`   | `#E6F5F2` |
| accent       | `cg_accent`       | `#E8A87C` |
| danger       | `cg_danger`       | `#E57373` |
| bg_app       | `cg_bg_app`       | `#F3F5F8` |
| text_primary | `cg_text_primary` | `#2C3340` |


圆角：12 / 16 / 24 dp 对应 sm / md / lg。禁止纯黑、紫蓝主视觉渐变。

---

## 8. 网络与数据架构

### 8.1 API 客户端

```
OkHttpClient
  ├─ AuthInterceptor（Authorization: Bearer <token>）
  ├─ LoggingInterceptor（仅 Debug）
  ├─ 超时：连接 10s / 读 20s
  └─ 证书：HTTPS 强制
Retrofit
  └─ GsonConverterFactory
```

统一响应包装（示例）：

```java
public class ApiResponse<T> {
    int code;
    String message;
    T data;
}
```

客户端将 HTTP/业务码映射为 `Resource<T>`：`Loading | Success | Error`。

### 8.2 会话


| 项            | 策略                                 |
| ------------ | ---------------------------------- |
| Access Token | 本地加密或至少私有 Prefs 存储；有效期按 PRD（如 7 天） |
| 刷新           | 401 时尝试 Refresh；失败清会话跳 Login       |
| 退出 / 注销      | 清 Token + 本地用户缓存；注销文案告知 30 天删除     |


### 8.3 一期模块 API 边界（客户端视角）


| 模块      | 能力                                            |
| ------- | --------------------------------------------- |
| Auth    | 发验证码、登录/注册、刷新、退出                              |
| User    | 资料、信用分、角色切换、校园卡上传                             |
| Task    | 草稿 CRUD、发布、列表、详情、抢单、开始配送、送达确认、发布者确认、加价、转紧急、取消 |
| Wallet  | 余额、流水、充值、提现、账户绑定                              |
| Message | 会话列表、历史消息、发送、已读                               |
| Notify  | 通知列表、已读                                       |


> 接口字段以服务端 OpenAPI 为准；客户端一期可用 MockWebServer / 本地 Mock Repository 并行开发 UI。

### 8.4 本地数据


| 存储    | 内容                                   |
| ----- | ------------------------------------ |
| Prefs | Token、guideShown、activeRole、deviceId |
| Room  | 发布草稿、可选：离线消息队列                       |


---

## 9. 关键子系统设计

### 9.1 抢单

1. 客户端防抖（按钮 loading，禁用重复点击）。
2. `POST /tasks/{id}/grab`。
3. 服务端行锁更新：`status=PENDING → ACCEPTED`。
4. 失败（已抢走）→ 提示「手慢无」，刷新详情。
5. 限流：遵循服务端策略；客户端避免疯狂重试（最多有限次）。

### 9.2 资金托管（客户端流程）

```
发布 / 加价 / 加入拼单
  → 查询余额
  → 不足 → W04 充值成功回传
  → 足够 → 服务端扣托管
任务 COMPLETED → 服务端划转跑腿员钱包
提现 → 校验 W05 已绑定 → 提交审核
```

一期支付可 **Mock 充值**；正式环境再接微信/支付宝 SDK。

### 9.3 配送定位

```
跑腿员点击「开始配送」
  → 启动 DeliveryLocationService（Foreground）
  → 通知栏：「正在配送中…」
  → 按状态分频上报（前往取件/附近/前往送达/附近，见 PRD）
  → 「我已送达」或任务取消/完成 → stopService
发布者 L01 订阅位置推送或轮询展示
```

隐私：仅配送期间采集；完成后停止；仅该单发布者可见。

### 9.4 拍照确认（T08）

- 仅 CameraX 实时拍摄，**不提供相册选择**。  
- 上传对象存储 → 回调任务进入 `CONFIRMING`。  
- 成功后返回 T06（跑腿员待确认态）。  
- 5 分钟内允许补拍（服务端窗口校验）。

### 9.5 即时通讯


| 阶段  | 方案                                    |
| --- | ------------------------------------- |
| 一期  | HTTPS 发消息 + 轮询 / 简单 WebSocket；支持文本、图片 |
| 二期  | 语音消息；可评估 IM SDK                       |
| 规则  | 接单后建会话；完成后 7 天归档只读                    |


### 9.6 地图


| 页面       | SDK 能力                |
| -------- | --------------------- |
| T05 地图选点 | POI 搜索、拖点、回填经纬度（高德坐标） |
| L01 追踪   | Marker + 轨迹线 + 预计到达   |
| L02 热力   | 热力图层 + 时段筛选（P2）       |


---

## 10. 安全与隐私


| 要求    | 客户端落地                               |
| ----- | ----------------------------------- |
| HTTPS | 全接口；禁止明文 HTTP（Debug 例外需明确开关）        |
| JWT   | 请求头携带；过期刷新；敏感页校验登录态                 |
| 日志    | Release 关闭 Body 日志；禁止打印 Token/手机号明文 |
| 脱敏展示  | 手机号、学号按产品规则打码                       |
| 权限最小化 | 相机、定位、通知按需申请；文案说明用途                 |
| 定位隐私  | 配送结束后停止采集与展示                        |
| 截屏    | 认证页、提现账户页可考虑 FLAG_SECURE（可选）        |


---

## 11. 非功能指标落地


| PRD 指标         | 客户端策略                       |
| -------------- | --------------------------- |
| 首屏 ≤ 2s        | 主壳懒加载 Tab；首页关键数据优先；图片压缩     |
| 接口 95% ≤ 500ms | 客户端侧：超时提示、骨架屏、避免主线程解析大 JSON |
| 抢单无超抢          | 服务端保证；客户端正确处理冲突码            |
| 消息 ≤ 1s        | 在线通道；失败重发 UI                |
| 位置展示 ≤ 3s      | 上报队列 + 详情/地图订阅刷新            |
| 弱网可用           | 关键写操作可重试；列表缓存；明确错误态         |


屏幕适配：约束布局 + dp；设计宽度参考 360dp；覆盖 320–480dp 宽度。

---

## 12. 分期交付与里程碑

### 12.1 一期 MVP（P0）— 打通闭环

**页面**：M01–M04、A01、T01（普通）、T02、T05、T06、T08、T11、C01（文本）、W01–W05、S01、S05  

**链路**：

```
登录 →（可选认证）→ 发单（含余额/充值）→ 抢单 → 聊天
→ 开始配送 → 拍照 → 详情内确认 → 钱包到账/提现（含账户绑定）
```

### 12.2 二期（P1）

拼单 T07、预约、紧急、评价 T10、模板/地址完善、加价/转紧急、投诉申诉、L01、S02–S04、S06、A03  

### 12.3 三期（P2）

A02 引导、L02 热力、D01 看板、G01/G02 积分签到、选券、推荐算法展示  

### 12.4 建议开发顺序（工程落地）

1. 工程初始化 + Theme + 空壳 Main
2. Auth（Login）+ Session
3. TaskDetail 四态 × 双角色（Mock）
4. Publish（普通任务）+ Tasks 列表
5. Wallet Mock + Verify
6. Chat + 深链
7. CameraX + Location Service + 地图选点
8. 联调真实 API
9. P1 / P2 模块

---

## 13. 质量保障

### 13.1 测试分层


| 层级           | 内容                                           |
| ------------ | -------------------------------------------- |
| 单元测试         | UseCase、状态机决策（`TaskDetailUiState` 映射）、Router |
| Instrumented | 关键 Fragment 导航、登录态                           |
| 手工走查         | 对照原型主路径 + 界面拆分演示参数表                          |


### 13.2 代码规范（摘要）

- 包名、类名遵循 Android 与 Java 惯例  
- 布局 ID：`snake_case`；Binding 自动生成  
- 禁止在 UI 线程做网络 / 重 IO  
- 新功能默认不扩大一期范围，需标注优先级

### 13.3 可观测性

- Debug：OkHttp Logging  
- 关键路径埋点预留：登录成功、发布成功、抢单成功/失败、确认完成（具体 SDK 后定）

---

## 14. 风险与决策记录


| 决策      | 结论                    | 原因              |
| ------- | --------------------- | --------------- |
| 客户端语言   | **Java**              | 团队熟悉度；XML 技术栈成熟 |
| UI 框架   | **XML + ViewBinding** | 非 Compose       |
| 架构      | **MVVM + 单 Activity** | 适配多状态详情与深链      |
| 服务端数据库  | **PostgreSQL**        | 见 `CampusGo-数据表结构设计.md`；JSONB/事务/扩展友好 |
| T09 确认页 | **不实现**               | 确认并入 T06        |
| 原型演示条   | **不进正式包**             | 仅 HTML 走查       |
| 一期支付    | **Mock 托管**           | 先闭环业务再接渠道       |
| 信用分     | **0–1000 / 初 500**    | 以 PRD 为准        |



| 风险            | 缓解                                 |
| ------------- | ---------------------------------- |
| 抢单超卖          | 服务端行锁 + 客户端冲突处理                    |
| 定位被系统杀后台      | Foreground Service + 厂商保活指引（合规范围内） |
| 高德 Key / 包名配置 | 分 Debug/Release 配置，文档化申请流程         |
| 范围膨胀          | 严格按分期；变更走评审                        |


---

## 15. 附录

### 15.1 页面编号速查（与界面拆分一致）


| 编号                      | 名称          | 一期    |
| ----------------------- | ----------- | ----- |
| M01–M04                 | 首页/任务/消息/我的 | ✓     |
| A01                     | 登录          | ✓     |
| T01/T02/T05/T06/T08/T11 | 发布链路与履约     | ✓     |
| C01                     | 聊天          | ✓（文本） |
| W01–W05                 | 钱包          | ✓     |
| S01/S05                 | 认证/设置       | ✓     |
| T07/T10/L01…            | 见二期         |       |
| A02/L02/D01/G01/G02     | 见三期         |       |


### 15.2 相关文档


| 文档                               | 用途           |
| -------------------------------- | ------------ |
| `CampusGo-PRD.md`                | 需求与业务规则      |
| `CampusGo-界面拆分.md`               | 页面、路由、角色 Tab |
| `CampusGo-功能拆解与交互规格.md`          | 控件级交互与绘页     |
| `CampusGo-SoftUI-DesignToken.md` | 视觉 Token |
| `CampusGo-数据表结构设计.md` | 服务端 **PostgreSQL** 表结构 |
| `CampusGo-技术架构文档.md` | **本文** |


### 15.3 修订记录


| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0 | 2026-07-27 | 首版：Java + MVVM + 单 Activity；对齐 PRD/界面拆分/原型结论 |
| v1.1 | 2026-07-27 | 明确服务端数据库为 **PostgreSQL**，与数据表设计文档对齐 |


---

> **文档结束** — 后续以本文搭建 `android/` 工程；若实现细节与本文冲突，先更新本文再改代码，避免架构漂移。

**工程状态（2026-07-27）**：仓库已初始化 `android/`（Java · Login → Main 四 Tab 骨架），用 Android Studio 打开该目录即可 Sync 运行。详见 `android/README.md`。

