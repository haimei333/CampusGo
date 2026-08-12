# CampusGo Trae / AI 绘页提示词（Soft UI 清洗版）

> 配合 `CampusGo-SoftUI-DesignToken.md` + `CampusGo-功能拆解与交互规格.md` 使用  

---

## A. 全局 System Prompt（每次对话先贴）

```
你是 Android 消费类 App UI 设计师与前端实现助手。正在为「CampusGo 校园跑腿」出界面。

必须严格遵守《CampusGo Soft UI Design Token》清洗版：
- 风格：柔和界面风 Soft UI（大圆角、柔和模糊阴影、低饱和、白卡片、浅灰蓝页面底）
- 主色 brand #3D9B8F；浅底 #E6F5F2；页面底 #F3F5F8；主文字 #2C3340（禁止 #000000）
- 卡片：白底、圆角 24dp、轻阴影；按钮圆角 16dp、主色填充；输入框灰底无描边、聚焦 brand 柔光环
- 字体：Noto Sans SC / 系统黑体，标题 Semibold，禁止 Inter/Roboto/Geist 作展示字，禁止整页等宽 mono，禁止 font-black
- 动效：200ms、按压缩放；禁止 bounce；手机以 ripple/press 为主

绝对禁止（出现即重画）：
- rounded-none、尖锐直角块
- border-black、border-2/4 粗黑框、硬位移阴影 shadow-[Npx_Npx_0px]
- 紫色到靛蓝色渐变主视觉、渐变文字、玻璃拟态当默认
- 卡片里再套同级大阴影卡片
- 高饱和纯色大面积、纯黑背景

信息架构与文案、控件、跳转以《功能拆解与交互规格》对应页面章节为准，不得删减关键字段。
出图设备：手机竖屏 360×780dp。每页标注可点击热区与跳转编号（如 → T06）。
输出前按 Design Token §11 自检清单逐条确认。
```

---

## B. 单页绘 Prompt 模板

把 `{PAGE_ID}` `{PAGE_NAME}` `{VARIANT}` `{EXTRA}` 替换后发送：

```
请绘制 CampusGo 页面【{PAGE_ID} {PAGE_NAME}】{VARIANT}。

1) 打开并严格遵循《功能拆解与交互规格》中该页的「结构树 + 文案 + 交互表」。
2) 视觉严格执行 Soft UI Design Token：青绿主色、圆角、柔阴影、浅灰蓝底、无黑框。
3) 交付：
   - 完整竖屏界面视觉稿（含状态栏与必要底栏）
   - 关键点击热区与跳转标注
   - 列表至少 2～3 条中文假数据
4) {EXTRA}

不要使用 Neo-Brutal / 黑粗边 / 紫蓝渐变风格。
```

### 常用 EXTRA 示例


| 页面  | EXTRA                                      |
| --- | ------------------------------------------ |
| M01 | 出两个变体：发布者身份、跑腿员身份                          |
| T01 | 出三个步骤屏：类型 / 填写 / 确认                        |
| T06 | 至少 5 态：待接单-发布者、待接单-跑腿员、配送中-发布者、待确认-发布者、已完成 |
| T07 | 展示未满员进度与成员列表                               |
| W01 | 突出余额大数字与流水列表                               |
| A01 | 居中品牌区 + 手机号验证码表单，亲和温暖                      |
| C01 | 气泡对话 + 底栏输入，圆角气泡                           |


---

## C. 分批任务 Prompt（可整段复制）

### 第 1 批 — 主框架

```
按 CampusGo Soft UI Token + 功能拆解规格，绘制：
MAIN 底栏、M01 首页（发布者+跑腿员两态）、M02 任务、M03 消息、M04 我的。
保持同一套组件：BottomNav、TaskCard、RoleSwitch、Fab「+ 发布任务」。
风格：柔和圆角青绿，禁止黑框硬阴影。
```

### 第 2 批 — 核心履约

```
绘制：A01 登录、T01 发布（三步）、T02 成功、T05 地图选点、T06 任务详情（5 个状态底栏）。
详情页是信息中枢，状态色柔和可辨，主按钮仅一个高强调。
```

### 第 3 批 — 资金与认证

```
绘制：T08 拍照确认、C01 聊天、W01–W05 钱包链路、S01 校园卡认证。
输入框灰底无边框；主按钮 #3D9B8F。
```

### 第 4 批 — 弹层与拼单

```
绘制：T07 拼单、T10 评价、T11 取消半屏、L01 追踪、SHEET-ADD 加价、DIALOG-AUTH、DIALOG-CREDIT。
半屏顶部大圆角 24dp + 遮罩。
```

### 第 5 批 — 其余

```
绘制：A02 引导、A03 资料、T03 模板、T04 地址、S02–S06、D01、L02、G01、G02。
风格与前几批完全一致，禁止风格漂移。
```

---

## D. 若让 AI 直接写代码（Compose / HTML Mock）

在全局 Prompt 后追加：

```
请用（Jetpack Compose / Tailwind 静态页）实现该界面。
- Compose：颜色与圆角使用 CampusGoTheme token，勿写死黑色边框。
- Tailwind Mock：按钮必须含 px-6 py-3 rounded-2xl font-medium shadow-lg transition-all duration-200 active:scale-95；
  卡片必须含 bg-white rounded-3xl shadow-xl shadow-gray-200/50 p-6；
  输入必须含 px-5 py-3.5 bg-gray-50 border-0 rounded-2xl focus:ring-2 focus:ring-[#3D9B8F]/50。
- class 中禁止：rounded-none、rounded-sm、border-black、border-2、border-4、bg-black、硬阴影任意值。
- 主色使用 bg-[#3D9B8F]，不要用 indigo/purple 作为主色。
```

---

## E. 风格漂移快速对照


| 看到这个        | 改成这个                           |
| ----------- | ------------------------------ |
| 黑粗框卡片       | 白卡片 + 柔阴影                      |
| indigo/紫色按钮 | `#3D9B8F`                      |
| 直角输入框 + 灰描边 | 灰底无边 + 聚焦 brand 环              |
| 纯黑大标题       | `#2C3340` Semibold             |
| 硬彩蛋阴影       | `shadow-lg shadow-gray-200/50` |
| 嵌套卡片阴影      | 外层卡片，内层用浅底分区无阴影                |


---

> 推荐工作流：先贴 **A 全局 Prompt** → 再贴 **B 单页** 或 **C 批次** → 出图后用 Token §11 自检 → 再进开发。

