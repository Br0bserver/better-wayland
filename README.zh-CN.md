# BetterWayland

[English](README.md) | [简体中文](README.zh-CN.md)

BetterWayland 是一个 Fabric 客户端模组，在保留 GLFW 作为窗口与输入后端的
前提下改善 Minecraft 的原生 Wayland 兼容性，不使用 SDL 替换 GLFW。

> [!IMPORTANT]
> BetterWayland 目前是面向 Linux x86_64 Wayland 会话的测试版。项目在
> `v0.1.0-beta.2` 及以前名为 WaylandFix；安装新版本前请删除旧的
> `waylandfix` JAR。

## 修复内容

- **原生 GLFW：** 在 Minecraft 创建窗口前选择 Wayland，并内置可复现构建、
  包含所需 LWJGL 输入法导出的 GLFW。启动时会校验原生库摘要；如果检测到
  冲突的外部 GLFW 覆盖，则会尽早报错，避免游戏在只应用部分修复的状态下
  继续运行。
- **窗口与全屏状态：** 设置稳定的 Wayland app ID，避免窗口显示时意外抢占
  焦点，并在全屏切换后重新同步逻辑尺寸与帧缓冲尺寸。原生补丁还修复了部分
  合成器上的分数缩放舍入和尺寸回调缺失问题。
- **光标行为：** 支持较新的 Wayland cursor-shape 协议，并为不支持新协议的
  合成器提供光标移动回退路径。
- **键盘状态：** Wayland 焦点丢失时释放 Minecraft 的按键映射，避免指针锁定
  或工作区切换后移动键持续按下；同时防止打开聊天或命令界面的按键被插入
  新打开的输入框。
- **26.x 原生输入法接入：** 将 Minecraft 文本输入焦点连接到 Wayland
  text-input-v3/v1，把候选框位置从帧缓冲坐标转换为逻辑表面坐标，并隐藏
  Minecraft 重复的浮动预编辑框，同时保留合成器提供的候选窗口。
- **26.x 组合输入生命周期：** 在界面、焦点输入控件或 Wayland 焦点变化时
  清除过期预编辑。仅当输入法确实吞掉相关文本按键时才隔离 Backspace、Enter
  和 Escape 等编辑键，因此正常英文输入与快捷键不会受到影响。
- **26.x 游戏输入法隔离：** 非文本输入状态下断开合成器输入上下文。即使游戏
  过程中误切换到中日韩输入法，移动键也不会再变成卡住的预编辑序列。

## 兼容性

- 需要 Fabric Loader 0.19.2 或更高版本。
- 支持 Linux x86_64 Wayland 会话。使用内置原生库时不要另外设置 GLFW
  覆盖。
- 明确不兼容 BorderlessFullscreen/FullscreenFix（`fullscreenfix`），其全屏
  模型与原生 Wayland 冲突。
- 不建议同时使用 IMBlocker。两个模组会拦截相同的输入法回调，可能造成重复
  预编辑框或候选框位置错误。

## 安装

从 [Releases](https://github.com/Br0bserver/wayland-fix-mod/releases) 下载与
Minecraft 构建版本对应的 JAR，删除旧版 WaylandFix，然后将 BetterWayland
放入实例的 `mods` 目录。JAR 声明的 Minecraft 范围不匹配时，Fabric Loader
会拒绝加载。

## 构建

```sh
./gradlew bundleNative buildAllVersions
```

构建产物位于 `versions/<version>-fabric/build/libs/`。

BetterWayland 使用 [LGPL-3.0-or-later](LICENSE) 许可证。
