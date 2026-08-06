# Bundled GLFW build

The beta native is built from pinned LWJGL-CI GLFW commit
`73a656a1dda93bc48b05ee7f923d587132965539` (the LWJGL IME support baseline)
and patchset commit `7388228a631956390741d41ba5ebabafe9ca9074`. It keeps both
X11 and Wayland support and preserves the GLFW ABI expected by LWJGL 3.4.1.
SDL is not involved.

On Debian/Ubuntu, install CMake, Ninja, Wayland, xkbcommon, Wayland protocols,
X11 development headers, and OpenGL development headers. Then run:

```sh
./gradlew bundleNative build
```

The script uses ignored repository caches and a fresh temporary worktree on
each run, so applying the mail patches is repeatable. One upstream CMake
context difference in the cursor-shape mail patch is normalized without
changing its source hunks. The script also checks that the
LWJGL-CI IME/preedit entry points are exported before copying the library.
The generated library, checksum, and source manifest are ignored by Git. Local
patch 0009 resets the Wayland text-input-v3/v1 composition when Minecraft moves
focus between text widgets, so the compositor cannot carry a candidate window
from the old widget into the new one.
Release CI must run this task before assembling a beta jar.
