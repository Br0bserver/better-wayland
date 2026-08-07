# Bundled GLFW build

The beta native is built from pinned LWJGL-CI GLFW commit
`73a656a1dda93bc48b05ee7f923d587132965539` (the LWJGL IME support baseline)
and patchset commit `7388228a631956390741d41ba5ebabafe9ca9074`. It keeps both
X11 and Wayland support and preserves the GLFW ABI expected by LWJGL 3.4.1.
SDL is not involved.

On Debian/Ubuntu, install CMake, Ninja, Wayland, xkbcommon, Wayland protocols,
X11 development headers, and OpenGL development headers. Then run:

```sh
./gradlew bundleNative buildAllVersions
```

The script uses ignored repository caches and a fresh temporary worktree on
each run, so applying the mail patches is repeatable. One upstream CMake
context difference in the cursor-shape mail patch is normalized without
changing its source hunks. The script also checks that the
LWJGL-CI IME/preedit entry points are exported before copying the library.
The generated library, checksum, and source manifest are ignored by Git. Local
patch 0009 resets the Wayland text-input-v3/v1 composition when Minecraft moves
focus between text widgets, so the compositor cannot carry a candidate window
from the old widget into the new one. Patch 0010 connects GLFW's IME input mode
to Wayland text-input enable/disable transactions, keeping fcitx/IBus detached
while Minecraft has no active text field.
Patch 0011 forwards committed Wayland IME characters through both GLFW character
callback variants, which is required by Minecraft 1.20.x's `char-mods` path.
Release CI must run this task before assembling beta jars. The native output is
shared by all version projects under `versions/`.
