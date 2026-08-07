# WaylandFix

WaylandFix is a client-only Fabric mod for Minecraft 26.1.x
(`26.1`, `26.1.1`, and `26.1.2`) and 26.2. It keeps GLFW as the window/input
backend and targets native Wayland compatibility without an SDL replacement.
The `v0.1.0-beta.2` release is the tested 26.1.2 baseline; ongoing
cross-version work is developed on `dev`.

The beta release contains a reproducibly built x86_64 glibc
`libglfw.so` based on LWJGL-CI/glfw. On a Wayland session, a missing, invalid,
or externally overridden GLFW is treated as a startup error instead of silently
running a partially fixed configuration.

## Known problem classes

The failures addressed by the project fall into separate layers:

- **Platform selection:** Minecraft 26.1.2 otherwise prefers X11. The client
  mixin enables GLFW's Wayland platform before the window is created.
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
  suppressing Minecraft's duplicate floating preedit box.
- **IME control-key leakage:** when an unmodified text key is consumed by the
  Linux IME without producing Minecraft's matching character callback,
  WaylandFix prevents the following IME editing/navigation key from also
  reaching the active text widget. English input resets the correlation as soon
  as its normal character callback arrives.
- **Stuck keys:** a focus loss during pointer-lock changes can omit release
  events. Wayland focus loss now releases Minecraft's key mappings.
- **IME focus lifetime:** the Java-side IME correlation state is reset when the
  active screen or focused widget changes, so a composition cannot leak into a
  later screen.
- **Gameplay IME isolation:** Minecraft's text-input focus now controls the
  Wayland text-input-v3/v1 context. Outside a text field the compositor IME is
  detached, so an IME toggle shortcut cannot turn movement keys into preedit;
  entering chat, search, books, or signs re-enables the existing input method.
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

The 26.1.2 artifact declares the Fabric-compatible `~26.1-` Minecraft range,
covering the `26.1` patch line. Jars are written to
`versions/<version>/build/libs/`. The native task always
builds once into the shared `src/main/resources/natives/linux-x86_64` resource
directory and both version projects package that same verified GLFW binary.
