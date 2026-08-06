#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cache_dir="${root_dir}/native/.cache"
output_dir="${root_dir}/src/main/resources/natives/linux-x86_64"
glfw_commit="73a656a1dda93bc48b05ee7f923d587132965539"
patch_commit="7388228a631956390741d41ba5ebabafe9ca9074"

command -v cmake >/dev/null || { echo "cmake is required" >&2; exit 127; }
command -v ninja >/dev/null || { echo "ninja is required" >&2; exit 127; }
command -v readelf >/dev/null || { echo "binutils (readelf) is required" >&2; exit 127; }

mkdir -p "${cache_dir}"
glfw_cache="${cache_dir}/glfw"
patch_cache="${cache_dir}/patches"
if [[ ! -d "${glfw_cache}/.git" ]]; then
  git clone --no-checkout https://github.com/LWJGL-CI/glfw "${glfw_cache}"
fi
if [[ ! -d "${patch_cache}/.git" ]]; then
  git clone --no-checkout https://github.com/jdkeke142/glfw-wayland-minecraft "${patch_cache}"
fi
if ! git -C "${glfw_cache}" cat-file -e "${glfw_commit}^{commit}" 2>/dev/null; then
  git -C "${glfw_cache}" fetch --depth 1 origin "${glfw_commit}"
fi
if ! git -C "${patch_cache}" cat-file -e "${patch_commit}^{commit}" 2>/dev/null; then
  git -C "${patch_cache}" fetch --depth 1 origin "${patch_commit}"
fi
git -C "${glfw_cache}" update-ref refs/waylandfix/glfw "${glfw_commit}"
git -C "${patch_cache}" update-ref refs/waylandfix/patches "${patch_commit}"

work_dir="$(mktemp -d "${TMPDIR:-/tmp}/waylandfix-glfw.XXXXXXXX")"
cleanup() {
  git -C "${glfw_cache}" worktree remove --force "${work_dir}/glfw" >/dev/null 2>&1 || true
  git -C "${patch_cache}" worktree remove --force "${work_dir}/patches" >/dev/null 2>&1 || true
  rm -rf -- "${work_dir}"
}
trap cleanup EXIT

git -C "${glfw_cache}" worktree add --detach "${work_dir}/glfw" "${glfw_commit}"
git -C "${patch_cache}" worktree add --detach "${work_dir}/patches" "${patch_commit}"

# 0001 drops every Ctrl/Alt character and breaks AltGr/IME layouts. Minecraft
# 26.1.2 uses the plain char callback, so Java-side correlation handles only
# consumed shortcuts/opening keys. Apply the remaining native fixes.
git -C "${work_dir}/glfw" \
  -c user.name=WaylandFix -c user.email=waylandfix@invalid \
  am \
  "${work_dir}/patches/0002-Implement-glfwSetCursorPos-with-fallback-for-older-c.patch" \
  "${work_dir}/patches/0003-Fix-window-size-callback-not-firing-on-unset-fullscr.patch" \
  "${work_dir}/patches/0004-Fix-framebuffer-size-rounding-with-fractional-scalin.patch"

# Patch 0005 was authored against a GLFW tree that indented this CMake block
# differently and used a commented #endif(). Normalize only that mail patch's
# context; the source changes remain byte-for-byte those from the pinned patch.
awk 'NR >= 1376 && NR <= 1383 {
    line = substr($0, 1, 1) substr($0, 6);
    if (line == " #endif()") line = " endif()";
    print line;
    next;
} { print }' \
  "${work_dir}/patches/0005-Implement-cursor-shape-v1-protocol.patch" \
  > "${work_dir}/patch5.mbox"
git -C "${work_dir}/glfw" apply --whitespace=nowarn "${work_dir}/patch5.mbox"
git -C "${work_dir}/glfw" add -A
git -C "${work_dir}/glfw" \
  -c user.name=WaylandFix -c user.email=waylandfix@invalid \
  commit -m "Apply cursor shape protocol compatibility" >/dev/null

git -C "${work_dir}/glfw" \
  -c user.name=WaylandFix -c user.email=waylandfix@invalid \
  am \
  "${work_dir}/patches/0006-Add-GLFW_FORCE_WAYLAND-env-var-to-override-app-reque.patch" \
  "${work_dir}/patches/0007-Add-GLFW_USE_LEGACY_CURSOR_WARP-env-var-to-opt-out-o.patch"

git -C "${work_dir}/glfw" apply \
  "${root_dir}/native/patches/0008-Clear-pending-Wayland-preedit-after-done.patch"
git -C "${work_dir}/glfw" apply \
  "${root_dir}/native/patches/0009-Reset-Wayland-preedit-on-focus-loss.patch"

cmake -S "${work_dir}/glfw" -B "${work_dir}/build" -G Ninja \
  -DGLFW_BUILD_WAYLAND=ON \
  -DGLFW_BUILD_X11=ON \
  -DBUILD_SHARED_LIBS=ON \
  -DGLFW_BUILD_EXAMPLES=OFF \
  -DGLFW_BUILD_TESTS=OFF \
  -DGLFW_BUILD_DOCS=OFF
cmake --build "${work_dir}/build" --parallel

library="$(find "${work_dir}/build" -type f -name 'libglfw.so*' -print | sort | head -n 1)"
if [[ -z "${library}" ]]; then
  echo "GLFW build did not produce libglfw.so" >&2
  exit 1
fi

required_symbols=(
  glfwSetPreeditCallback
  glfwSetIMEStatusCallback
  glfwSetPreeditCandidateCallback
  glfwGetPreeditCursorRectangle
  glfwSetPreeditCursorRectangle
  glfwResetPreeditText
  glfwGetPreeditCandidate
)
for symbol in "${required_symbols[@]}"; do
  if ! readelf --dyn-syms --wide "${library}" | awk -v wanted="${symbol}" '$8 == wanted { found = 1 } END { exit found ? 0 : 1 }'; then
    echo "bundled GLFW is missing required LWJGL symbol: ${symbol}" >&2
    exit 1
  fi
done

mkdir -p "${output_dir}"
cp "${library}" "${output_dir}/libglfw.so"
sha256sum "${output_dir}/libglfw.so" > "${output_dir}/libglfw.so.sha256"
cat > "${output_dir}/libglfw.manifest" <<EOF
source=LWJGL-CI/glfw
sourceCommit=${glfw_commit}
patches=jdkeke142/glfw-wayland-minecraft
patchCommit=${patch_commit}
localPatches=native/patches/0008-Clear-pending-Wayland-preedit-after-done.patch,native/patches/0009-Reset-Wayland-preedit-on-focus-loss.patch
architecture=linux-x86_64
requiredSymbols=${required_symbols[*]}
EOF
