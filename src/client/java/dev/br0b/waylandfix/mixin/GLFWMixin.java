package dev.br0b.waylandfix.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** GLFW operations which have no meaningful Wayland equivalent. */
@Mixin(value = GLFW.class, remap = false)
public abstract class GLFWMixin {
    @Inject(method = "glfwSetWindowPos(JII)V", at = @At("HEAD"), cancellable = true)
    private static void waylandfix$ignoreUnsupportedWindowPosition(long window, int x, int y, CallbackInfo callback) {
        if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
            callback.cancel();
        }
    }

    @Inject(method = "glfwGetWindowPos(J[I[I)V", at = @At("HEAD"), cancellable = true)
    private static void waylandfix$provideUnknownWindowPosition(long window, int[] x, int[] y, CallbackInfo callback) {
        if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
            if (x != null && x.length > 0) {
                x[0] = 0;
            }
            if (y != null && y.length > 0) {
                y[0] = 0;
            }
            callback.cancel();
        }
    }
}
