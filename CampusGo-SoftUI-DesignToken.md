# CampusGo Soft UI Design Token（清洗版）

> **风格**: 柔和界面风（soft-ui）  
> **适用**: CampusGo 校园跑腿 App 全端绘页 / Compose / XML / Trae 出图  
> **原则**: 以本文为准；忽略外部 Stylekit 中黑粗框、硬阴影、`rounded-none`、`font-black` 等冲突规则  

---

## 1. 品牌与色彩

### 1.1 色板（禁止纯黑 `#000000`、禁止紫蓝渐变主视觉）


| Token                 | Hex       | 用途                 |
| --------------------- | --------- | ------------------ |
| `color.brand`         | `#3D9B8F` | 主按钮、选中 Tab、关键链接、进度 |
| `color.brand.pressed` | `#34857B` | 按下                 |
| `color.brand.soft`    | `#E6F5F2` | 浅底标签、选中芯片、认证条背景    |
| `color.accent`        | `#E8A87C` | 次强调（签到、积分、温和提醒）    |
| `color.accent.soft`   | `#FFF1E6` | 次强调浅底              |
| `color.danger`        | `#E57373` | 错误、投诉、危险操作（低饱和红）   |
| `color.danger.soft`   | `#FDECEC` | 危险浅底               |
| `color.warning`       | `#F0B429` | 待确认、审核中            |
| `color.success`       | `#5BAE7A` | 成功、已完成             |
| `color.emergency`     | `#E8896B` | 紧急任务标签（暖橙，非高饱和纯红）  |


### 1.2 中性色


| Token                  | Hex                  | 用途               |
| ---------------------- | -------------------- | ---------------- |
| `color.bg.app`         | `#F3F5F8`            | 页面背景（柔和灰蓝，非纯白铺满） |
| `color.bg.card`        | `#FFFFFF`            | 卡片、底栏、顶栏         |
| `color.bg.input`       | `#F0F2F5`            | 输入框默认底           |
| `color.text.primary`   | `#2C3340`            | 主文案（替代纯黑，对比度达标）  |
| `color.text.secondary` | `#6B7380`            | 次要说明             |
| `color.text.tertiary`  | `#9AA3B2`            | 占位、时间戳           |
| `color.text.onBrand`   | `#FFFFFF`            | 主按钮字             |
| `color.divider`        | `#E8ECF1`            | 轻分割（优先留白，少用线）    |
| `color.overlay`        | `rgba(44,51,64,0.4)` | 弹层遮罩             |


### 1.3 语义标签色（Tag）


| 标签  | 底                                              | 字         |
| --- | ---------------------------------------------- | --------- |
| 普通  | `#EEF1F5`                                      | `#5A6575` |
| 拼单  | `#E6F5F2`                                      | `#2F7A70` |
| 紧急  | `#FFF0EA`                                      | `#C45C3E` |
| 预约  | `#EEF0FF` → **改用** `#EAF0FB` / `#4A6FA5`（避免紫系） |           |
| 草稿  | `#F5F5F5`                                      | `#8A9099` |


### 1.4 焦点 / 环（替代 indigo）

```
focus.ring = brand @ 40% 透明度
例: rgba(61, 155, 143, 0.45)
```

---

## 2. 圆角


| Token         | dp / rem | Tailwind 对照    | 场景           |
| ------------- | -------- | -------------- | ------------ |
| `radius.sm`   | 12dp     | `rounded-xl`   | 小芯片、Tag      |
| `radius.md`   | 16dp     | `rounded-2xl`  | 按钮、输入框、底栏图标底 |
| `radius.lg`   | 24dp     | `rounded-3xl`  | 卡片、弹层顶角、大模块  |
| `radius.full` | 999      | `rounded-full` | 头像、FAB、签到圆点  |


**禁止**: `rounded-none`、`rounded-sm`、直角硬切。

---

## 3. 阴影（柔和模糊，禁止硬位移阴影）


| Token           | Android / 描述     | CSS / Tailwind 示意              |
| --------------- | ---------------- | ------------------------------ |
| `elevation.1`   | 2dp，y=1，黑 6%     | `shadow-md shadow-gray-200/50` |
| `elevation.2`   | 6dp，y=2，黑 8%     | `shadow-lg shadow-gray-200/50` |
| `elevation.3`   | 12dp，y=4，黑 10%   | `shadow-xl shadow-gray-200/50` |
| `elevation.fab` | 8dp + brand 柔光可选 | FAB 专用                         |


**禁止**: `shadow-[4px_4px_0px_…]` 硬边阴影、`border-black` 代替层次。

**列表卡建议**: 默认 `elevation.1`；详情大卡 `elevation.2`；勿层层套卡再叠大阴影。

---

## 4. 边框

- 默认 **无可见描边**（`border-0`），靠背景色差 + 阴影分层。  
- 需要分隔时用 `color.divider` 的 **1px 浅线** 或留白，不用粗黑框。  
- **禁止**: `border-2/4`、`border-black`、单侧粗强调条。

---

## 5. 字体

### 5.1 字体族（避开 Inter / Roboto / Geist 作为展示主字）


| 端             | 推荐                                                                                            |
| ------------- | --------------------------------------------------------------------------------------------- |
| Android       | 中文：系统 `sans-serif` 可接受；展示标题可用 **「思源黑体 / Noto Sans SC」** Medium–Semibold；数字金额可用稍紧的等宽感但勿整页 mono |
| 绘页 / Web Mock | `"Noto Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif`                                |


字重：


| 用途       | 字重             |
| -------- | -------------- |
| 大标题      | Semibold (600) |
| 小标题 / 按钮 | Medium (500)   |
| 正文       | Regular (400)  |


**禁止**: 标题 `font-black` / 900；正文整页 `font-mono`。

### 5.2 字号（手机优先）


| Token          | sp / 类名          | 场景        |
| -------------- | ---------------- | --------- |
| `type.hero`    | 28–32sp          | 余额大数字、成功页 |
| `type.h1`      | 22–24sp          | 页标题（少用）   |
| `type.h2`      | 18–20sp          | 模块标题      |
| `type.h3`      | 16–17sp          | 卡片标题      |
| `type.body`    | 14–15sp          | 正文        |
| `type.caption` | 12sp             | 辅助、时间     |
| `type.amount`  | 20–28sp Semibold | 酬劳、余额     |


行宽：说明类正文尽量 ≤ 75 汉字宽（窄屏自然换行即可）。

---

## 6. 间距


| Token               | 值             | 用途           |
| ------------------- | ------------- | ------------ |
| `space.page.x`      | 16–20dp       | 页左右边距        |
| `space.section.y`   | 16–24dp       | 模块间距         |
| `space.card`        | 16–20dp       | 卡片内边距        |
| `space.list.gap`    | 10–12dp       | 列表卡片间距       |
| `space.bottom.nav`  | 56–64dp       | 底栏高度         |
| `space.bottom.safe` | 内容底部留白 ≥ 80dp | 避免被 FAB/底栏遮挡 |


**禁止**: 元素贴边挤成一团；卡片内边距 < 12dp。

---

## 7. 组件规格（REQUIRED）

### 7.1 主按钮 Primary

```
高度: 48dp
内边距: 水平 24dp
圆角: radius.md (16dp)
背景: color.brand
文字: color.text.onBrand · Medium · 15–16sp
阴影: elevation.2
按下: scale 0.96–0.97 + brand.pressed（Android ripple）
禁用: 背景 brand @ 40% 透明或 #B7D9D3，无强阴影
过渡: 200ms ease（禁 bounce/elastic）
```

Tailwind 示意（Mock 用）:

```
px-6 py-3 rounded-2xl font-medium shadow-lg
bg-[#3D9B8F] text-white
active:scale-95 transition-all duration-200
hover:-translate-y-0.5 hover:shadow-xl
```

（真机以 press/ripple 为准，hover 仅桌面预览。）

### 7.2 次按钮 Secondary

```
背景: white
文字: brand
阴影: elevation.1
或浅底 brand.soft + brand 字（无描边优先）
```

### 7.3 危险文字按钮

```
无填充 · text danger · 用于「退出登录」「注销」
配确认弹窗，不直接毁掉
```

### 7.4 卡片 Card

```
背景: white
圆角: radius.lg (24dp)
内边距: space.card
阴影: elevation.1～2
悬停/按压: 阴影略增 + 上移 1–2dp（列表可减弱，避免刷屏晃动）
禁止: 卡片内再套同样大阴影的卡片（嵌套卡片）
```

### 7.5 输入框 Input

```
高度: ≈48–52dp
内边距: 水平 20dp 垂直 14dp
背景: color.bg.input
圆角: radius.md
边框: 无
聚焦: 背景转白 + ring 2px brand/45%
占位符: text.tertiary
错误: 底 danger.soft + 下方 caption danger 文案
```

### 7.6 Tag / Chip

```
圆角: radius.sm 或 full
字号: 11–12sp Medium
内边距: 水平 10dp 垂直 4dp
无描边
```

### 7.7 底栏 BottomNav

```
背景: white · 顶部分割极淡或 elevation.1
高度: 56–64dp + 系统安全区
图标描线 24dp · 未选 text.tertiary · 选中 brand
选中可用 brand.soft 圆角胶囊衬底（勿粗框）
```

### 7.8 FAB

```
尺寸: 56dp 圆
背景: brand · 白字或「+」
阴影: elevation.fab
文案型 FAB 可用胶囊「+ 发布任务」圆角 full
```

### 7.9 顶栏 TopBar

```
背景: bg.app 或白（二级页建议白 + 轻阴影）
标题: h3 Semibold · text.primary
返回: 简洁箭头，热区 ≥ 44dp
禁止: 黑底顶栏、粗底边框
```

### 7.10 任务卡片 TaskCard

```
白底 rounded-3xl p-4
左: 标题(h3) + Tag 行 + 次要信息(caption)
右: 金额(type.amount · brand 或 primary)
按下微缩放，勿夸张上浮
紧急: 左侧可加 3–4dp brand/emergency 软色条（禁止粗黑边）——若与「禁止单侧粗边」冲突，则改用左上角紧急 Tag，不加色条
【CampusGo 约定】紧急仅用 Tag，不加侧边条
```

### 7.11 半屏 / 弹窗

```
圆角顶: 24dp
遮罩: overlay
把手条: 灰色圆角条 32×4
按钮区遵循主/次按钮规格
```

---

## 8. 动效


| 场景     | 规格                         |
| ------ | -------------------------- |
| 通用过渡   | 200–300ms `ease-out`       |
| 按压     | `scale(0.96)`              |
| 页面切换   | 系统默认横向/淡入即可                |
| **禁止** | bounce、elastic、过度弹跳；无限闪烁   |
| 无障碍    | 尊重系统「减少动态效果」：关闭位移，仅保留淡入或瞬切 |


---

## 9. Android 落地对照（Compose 示意）

```kotlin
object CampusGoTheme {
    val Brand = Color(0xFF3D9B8F)
    val BrandSoft = Color(0xFFE6F5F2)
    val Accent = Color(0xFFE8A87C)
    val BgApp = Color(0xFFF3F5F8)
    val BgCard = Color(0xFFFFFFFF)
    val BgInput = Color(0xFFF0F2F5)
    val TextPrimary = Color(0xFF2C3340)
    val TextSecondary = Color(0xFF6B7380)
    val TextTertiary = Color(0xFF9AA3B2)
    val Danger = Color(0xFFE57373)
    val RadiusMd = 16.dp
    val RadiusLg = 24.dp
}
```

XML: 在 `colors.xml` / `dimens.xml` / `themes.xml` 建同名资源；按钮 style 引用圆角 shape + elevation。

---

## 10. 页面背景配方

```
Scaffold 背景 = color.bg.app
主内容区卡片 = color.bg.card
首页顶部可轻微垂直渐变: bg.app → 略浅（非紫蓝、非高饱和）
成功页/钱包余额区可用 brand.soft 大圆角区块衬底，勿整页高饱和铺色
```

---

## 11. 自检清单（交付前）

### Soft UI 合规

- 圆角 ≥ 12dp，无 `rounded-none` / 直角块
- 无 `border-black`、无硬阴影、无纯黑 `#000`
- 无紫→靛主渐变、无渐变文字
- 无玻璃拟态当默认、无卡片套卡片叠阴影
- 主色为青绿 brand，焦点环同色系
- 按钮 / 卡片 / 输入框符合 §7
- 正文对比度 ≥ WCAG AA（深灰字配浅底）
- 无 bounce 动效；有减少动态备选

### CampusGo 业务

- 紧急/成功/警告用语义色，不靠高饱和纯红纯绿刺眼
- 列表可扫读：金额、状态、距离一眼可见
- 底栏 + FAB 不挡主按钮

---

## 12. 与外部 Stylekit 冲突时的裁决


| 外部规则                           | 裁决           |
| ------------------------------ | ------------ |
| 黑粗框导航 / Hero 模板                | **不用**       |
| 自检要求 rounded-none、border-black | **作废**       |
| indigo focus                   | **改为 brand** |
| 硬阴影 neo-brutal                 | **作废**       |
| Soft REQUIRED 按钮/卡片/输入         | **保留并本文化**   |


---

> **本文是 CampusGo 视觉唯一真相来源。** Trae / Cursor 出图与写码时，请同时附带下一文档中的「绘页提示词」。

