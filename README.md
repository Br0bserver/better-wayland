# BetterWayland

[English](README.md) | [简体中文](README.zh-CN.md)

BetterWayland is a client-side Fabric mod that makes Minecraft work properly on
native Wayland while keeping GLFW as the window and input backend. It does not
replace GLFW with SDL.

> [!IMPORTANT]
> BetterWayland is a beta for Linux x86_64 Wayland sessions. The project was
> called WaylandFix through `v0.1.0-beta.2`; remove the old `waylandfix` jar
> before installing this version.

## What it fixes

- **Native GLFW:** selects Wayland before Minecraft creates its window and
  bundles a reproducibly built GLFW with the required LWJGL IME exports. The
  native is checksum-verified at startup; conflicting external GLFW overrides
  fail early instead of leaving a partially patched game.
- **Window and fullscreen state:** supplies a stable Wayland app ID, avoids
  focus-on-show surprises, and resynchronizes logical and framebuffer sizes
  after fullscreen transitions. Native patches also correct fractional-scale
  rounding and missing size callbacks on affected compositors.
- **Cursor behavior:** adds modern Wayland cursor-shape support and a cursor-warp
  fallback for compositors that do not provide the newer protocol.
- **Keyboard state:** releases Minecraft key mappings when Wayland focus is
  lost, preventing movement keys from remaining held after pointer-lock or
  workspace transitions. It also prevents the key used to open chat or commands
  from being inserted into the newly opened field.
- **Native IME integration on 26.x:** connects Minecraft's text-input focus to
  Wayland text-input-v3/v1, converts candidate positions from framebuffer to
  logical surface coordinates, and suppresses Minecraft's duplicate floating
  preedit overlay while leaving the compositor candidate window intact.
- **Composition lifecycle on 26.x:** clears stale preedit when the screen,
  focused widget, or Wayland focus changes. Editing keys such as Backspace,
  Enter, and Escape are isolated only when an IME has consumed the related text
  key, so normal English input and shortcuts continue to work.
- **Gameplay IME isolation on 26.x:** detaches the compositor input context
  outside text fields. Accidentally switching to a CJK input method during
  gameplay therefore cannot turn movement keys into a stuck preedit sequence.

## Compatibility

- Requires Fabric Loader 0.19.2 or newer.
- Supports Linux x86_64 Wayland sessions. Do not configure an external GLFW
  override while using the bundled native.
- Intentionally incompatible with BorderlessFullscreen/FullscreenFix
  (`fullscreenfix`), whose fullscreen model conflicts with native Wayland.
- Using IMBlocker at the same time is not recommended. Both mods intercept the
  same IME callbacks, which can duplicate preedit or misplace candidates.

## Installation

Download the jar matching your Minecraft build from
[Releases](https://github.com/Br0bserver/better-wayland/releases), remove any
old WaylandFix jar, and place BetterWayland in the instance's `mods` directory.
Fabric Loader will reject a jar whose declared Minecraft range does not match.

## Building

```sh
./gradlew bundleNative buildAllVersions
```

Jars are written to `versions/<version>-fabric/build/libs/`.

BetterWayland is licensed under the [LGPL-3.0-or-later](LICENSE).
