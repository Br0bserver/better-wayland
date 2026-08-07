# Third-party notices

## GLFW

BetterWayland release jars contain a modified `libglfw.so` built from the
[LWJGL-CI GLFW fork](https://github.com/LWJGL-CI/glfw), which is based on
[GLFW](https://github.com/glfw/glfw). The exact source and patch revisions are
pinned in `native/build-glfw.sh`, and the resulting library preserves the
LWJGL 3.4.1 GLFW ABI, including its IME/preedit extensions.

GLFW is distributed under the zlib/libpng license:

Copyright (c) 2002-2006 Marcus Geelnard

Copyright (c) 2006-2019 Camilla Löwy

This software is provided 'as-is', without any express or implied
warranty. In no event will the authors be held liable for any damages
arising from the use of this software.

Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not
   claim that you wrote the original software. If you use this software
   in a product, an acknowledgment in the product documentation would
   be appreciated but is not required.

2. Altered source versions must be plainly marked as such, and must not
   be misrepresented as being the original software.

3. This notice may not be removed or altered from any source
   distribution.

## Related projects

Wayland behavior and patch design were informed by these projects. They are
listed for attribution and are not bundled as separate binaries:

- [jdkeke142/glfw-wayland-minecraft](https://github.com/jdkeke142/glfw-wayland-minecraft)
- [wired-tomato/WayGL](https://github.com/wired-tomato/WayGL) (MIT)
- [not-coded/WayFix](https://github.com/not-coded/WayFix) (LGPL-2.1)

No third-party binary is committed to source control. Release builds generate
the native library from pinned source and patches.
