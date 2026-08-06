# Third-party notices

The bundled `libglfw.so` is built from the LWJGL-CI GLFW fork, which is based
on GLFW and distributed under the zlib/libpng license. The build is intended
to preserve the LWJGL 3.4.1 GLFW ABI, including its IME/preedit extensions.

Wayland behavior and patch design are informed by:

- LWJGL-CI/glfw
- glfw/glfw
- jdkeke142/glfw-wayland-minecraft
- wired-tomato/WayGL (MIT)
- not-coded/WayFix (LGPL-2.1)

No third-party binary is committed to source control. Release builds generate
the native library from pinned source and patches.
