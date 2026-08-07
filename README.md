# WaylandFix

WaylandFix is a client-only Fabric mod for selected Minecraft releases from
1.20 through 26.2. It keeps GLFW as the window/input backend and targets native
Wayland compatibility without an SDL replacement. The `v0.1.0-beta.2` release
is the tested 26.1.2 baseline; ongoing cross-version work is developed on
`dev`.

Artifacts are grouped by Minecraft protocol version. Each build node targets
the newest release in its protocol group and declares only the releases that
share that protocol:

| Protocol | Build target | Declared Minecraft versions |
| ---: | --- | --- |
| 763 | 1.20.1 | 1.20-1.20.1 |
| 765 | 1.20.4 | 1.20.3-1.20.4 |
| 766 | 1.20.6 | 1.20.5-1.20.6 |
| 767 | 1.21.1 | 1.21-1.21.1 |
| 768 | 1.21.3 | 1.21.2-1.21.3 |
| 769 | 1.21.4 | 1.21.4 |
| 770 | 1.21.5 | 1.21.5 |
| 772 | 1.21.8 | 1.21.7-1.21.8 |
| 773 | 1.21.10 | 1.21.9-1.21.10 |
| 774 | 1.21.11 | 1.21.11 |
| 775 | 26.1.2 | 26.1.x |
| 776 | 26.2 | 26.2.x |

Protocols 764 (1.20.2) and 771 (1.21.6) currently have no artifact.

The beta release contains a reproducibly built x86_64 glibc
`libglfw.so` based on LWJGL-CI/glfw. On a Wayland session, a missing, invalid,
or externally overridden GLFW is treated as a startup error instead of silently
running a partially fixed configuration.

## Known problem classes

The failures addressed by the project fall into separate layers:

- **Platform selection:** supported Minecraft releases may otherwise select
  X11. The client mixin enables GLFW's Wayland platform before the window is
  created.
- **Native ABI:** a system GLFW can be ABI-compatible yet still lack LWJGL-CI's
  IME/preedit exports. `KeyboardHandler.setup` resolves those callbacks during
  startup, so the bundled beta verifies and preloads the required symbols.
- **Text input ordering:** Wayland can deliver the key and character callbacks
  after Minecraft has opened the chat screen. The Java state machine consumes
  only the character correlated with the chat/command key, including remapped
  keys, and leaves later IME text untouched.
- **Fullscreen and scaling:** some compositors do not emit a final logical-size
  callback when leaving fullscreen. The window mixin rereads logical and
  framebuffer sizes after `glfwSetWindowMonitor`, before Minecraft recalculates
  its GUI scale.
- **IME candidate positioning:** Minecraft passes framebuffer-scaled GUI
  coordinates directly to GLFW. WaylandFix converts the text-input rectangle to
  logical surface coordinates and keeps the native candidate window while
  suppressing Minecraft's duplicate floating preedit box. This integration is
  available on 26.x, where Minecraft exposes its native text-input pipeline.
- **IME control-key leakage:** when an unmodified text key is consumed by the
  Linux IME without producing Minecraft's matching character callback,
  WaylandFix prevents the following IME editing/navigation key from also
  reaching the active text widget. English input resets the correlation as soon
  as its normal character callback arrives.
- **Stuck keys:** a focus loss during pointer-lock changes can omit release
  events. Wayland focus loss now releases Minecraft's key mappings.
- **IME focus lifetime:** the Java-side IME correlation state is reset when the
  active screen or focused widget changes, so a composition cannot leak into a
  later screen. This integration is available on 26.x.
- **Gameplay IME isolation:** Minecraft's text-input focus now controls the
  Wayland text-input-v3/v1 context. Outside a text field the compositor IME is
  detached, so an IME toggle shortcut cannot turn movement keys into preedit;
  entering chat, search, books, or signs re-enables the existing input method.
  This integration is available on 26.x.
- **Cursor and event-loop behavior:** the native patchset covers cursor-warp
  fallback, fullscreen size callbacks, fractional framebuffer rounding, cursor
  shape, and explicit Wayland selection. Event-loop/swap stalls remain native
  compositor-specific and are tracked for a later patchset.

### Known incompatible mods

WaylandFix is intentionally incompatible with BorderlessFullscreen/FullscreenFix
(`fullscreenfix`). That mod replaces Minecraft's fullscreen state handling and
its borderless mode assumes X11-style client window positioning, which Wayland
compositors do not provide. Its startup exclusive-fullscreen path can also ask
for fullscreen before the first surface map, leaving a running game with no
visible window on niri. Fabric Loader rejects the combination instead of
silently applying a partial compatibility workaround.

### Not recommended together

Using IMBlocker together with WaylandFix on Linux is not recommended. WaylandFix
contains no IMBlocker-specific code and does not declare it incompatible, but
IMBlocker intercepts the same preedit callbacks and replaces Minecraft's native
candidate-area handling. This bypasses WaylandFix's logical-coordinate fix and
can reintroduce a duplicate preedit overlay or incorrectly positioned candidate
window. Disable IMBlocker when using WaylandFix's native Wayland text input.

The bundled GLFW also clears its transactional text-input-v3 preedit state after
each `done` event, preventing a commit-only batch from leaving stale preedit
text visible.

The upstream patch that globally drops Ctrl/Alt characters is intentionally not
included: it breaks AltGr and IME layouts. WaylandFix instead isolates only
specific editing/navigation keys after detecting a key/character callback
mismatch; letters, modifiers, AltGr, and normal shortcut handling remain
untouched.

## Build

The shared implementation lives in `src/`. Version-specific projects under
`versions/` use the ReplayMod/Fallen-Breath preprocessor, so API differences
stay in one source tree using directives such as `//#if MC >= 260200`.

Build the stable baseline:

```sh
./gradlew bundleNative :26.1.2-fabric:build
```

Build every configured development target:

```sh
./gradlew bundleNative buildAllVersions
```

Jars are written to `versions/<version>/build/libs/`. The native task always
builds once into the shared `src/main/resources/natives/linux-x86_64` resource
directory and every version project packages that same verified GLFW binary.

Minecraft 1.20.x and 1.21.x receive the GLFW/window, fullscreen, focus, and
chat-opening character fixes. The 26.x builds additionally contain the native
IME/preedit, candidate positioning, and text-focus integration because those
Minecraft and LWJGL Java APIs do not exist in the older releases.
