# BigFish Pet (大肥鱼桌宠) — 隐藏式控制中心实施方案

在现有纯净桌宠基础上，新增**隐藏式控制中心与项目设置页面**。默认保持纯黑全屏大肥鱼桌面，用户长按大肥鱼约 1000ms 后平滑淡入白色控制中心，并支持点击进入「项目设置」页面与原生手势/物理按键返回。

## User Review & Critical Decisions

> [!IMPORTANT]
> 针对您的交互、视觉与手势要求，已确立以下核心技术方案：

- **1000ms 精准长按与防误触手势系统**：
  - 使用 Compose `pointerInput` + `awaitEachGesture` 自定义手势识别引擎：
    - **触碰按下（Down）**：记录起始坐标，启动 1000ms 计时器与轻微触感震动。
    - **位移取消（Movement Slop）**：若手指移动超过系统防抖阈值（Touch Slop，约 16dp），立即取消长按识别，避免误触。
    - **长按达成（>= 1000ms）**：触发强烈长按触感震动，标记 `longPressTriggered = true`，平滑切换至控制中心；随后手指抬起时**绝对不触发普通点击**。
    - **短按抬起（Up, < 1000ms）**：若未达 1000ms 抬起，取消长按计时，立即执行原有**物理 Q 弹反馈 + 表情互动切换**。
- **纯白极简控制中心视觉规范**：
  - 背景：`#F8F9FA` / `#FFFFFF` 清爽极简浅色。
  - 单一功能入口：卡片式**「项目设置」**（配备精致设置齿轮图标与右侧导航箭头），无任何多余装饰或无关菜单。
  - 顶部配备返回桌宠按钮，并提供系统 `BackHandler`（全面屏侧滑返回、物理返回键均平滑退回黑色桌宠界面）。
- **平滑双向淡入淡出（Crossfade 250ms）**：
  - 使用 `AnimatedContent` 配合 `fadeIn(tween(250))` 与 `fadeOut(tween(250))`，避免闪白、黑边残影或掉帧。
  - 切换至控制中心时，系统栏无缝适配浅色模式；返回桌宠时立即恢复纯黑沉浸式隐藏状态栏与导航栏。
- **项目设置页面结构**：
  - 简洁独立的二级页面，顶部导航栏带返回箭头与标题「项目设置」，预留干净架构，后续可随时扩展。

---

## 1. Overview & Core Concept

- **交互入口**：桌宠界面中央的大肥鱼本身就是隐藏式进入钥匙，长按 1 秒开启控制中心。
- **沉浸感保护**：默认不露出任何按钮或设置齿轮，保障桌宠 100% 视觉纯净度。

---

## 2. User Experience & Visual Design

### Key User Flows
1. **纯黑桌宠状态（默认）**：全屏纯黑，大肥鱼居中呼吸待机；轻点产生物理 Q 弹与表情互动。
2. **长按 1000ms 呼出**：长按大肥鱼，震动确认后黑色背景与大肥鱼在 250ms 内柔和淡出，白色控制中心淡入。
3. **控制中心浏览**：白色卡片上只有「项目设置」一项，点击后推入设置页面。
4. **返回桌宠**：按系统返回键或界面返回按钮，250ms 淡回纯黑桌宠画面，大肥鱼恢复待机呼吸与点击能力。

---

## 3. Technical Architecture & Navigation

```
┌────────────────────────────────────────────────────────┐
│                      MainActivity                      │
│  - AppScreen State: PET | CONTROL_CENTER | SETTINGS    │
│  - System Bars Controller (Hide on PET, Light on CC)   │
│  - BackHandler (Hardware back key navigation)          │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│            AnimatedContent (Crossfade 250ms)           │
├──────────────────────────┬─────────────────────────────┤
│                          │                             │
▼                          ▼                             ▼
AppScreen.PET       AppScreen.CONTROL_CENTER      AppScreen.SETTINGS
(纯黑大肥鱼桌宠)       (极简白控制中心)              (项目设置页面)
- 1000ms长按识别      - 「项目设置」单入口           - 二级页面与返回
- 原版动画与Q弹        - 250ms淡入淡出
```

### Components to Create / Modify
1. **`AppScreen.kt`**: 屏幕路由状态枚举 (`PET`, `CONTROL_CENTER`, `SETTINGS`)。
2. **`PetGestureDetector.kt`**: 精准 1000ms 长按判定与短按点击分流手势修饰符。
3. **`ControlCenterScreen.kt`**: 极简白色控制中心组件（含「项目设置」条目）。
4. **`ProjectSettingsScreen.kt`**: 项目设置骨架页。
5. **`MainActivity.kt`**: 全局导航状态、`AnimatedContent` 页面切换与系统栏状态联动。
6. **单元测试**: 覆盖长按不触发点击、屏幕状态切换与返回行为。
