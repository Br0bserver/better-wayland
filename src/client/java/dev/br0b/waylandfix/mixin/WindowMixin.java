package dev.br0b.waylandfix.mixin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.GpuBackend;
import net.minecraft.client.KeyMapping;
import net.minecraft.server.packs.PackResources;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Window.class)
public abstract class WindowMixin {
    @Shadow
    @Final
    private long handle;

    @Shadow
    private int framebufferWidth;

    @Shadow
    private int framebufferHeight;

    @Shadow
    public abstract void setWidth(int width);

    @Shadow
    public abstract void setHeight(int height);

    @Inject(method = "createGlfwWindow", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuBackend;setWindowHints()V", shift = At.Shift.AFTER, remap = false))
    private static void waylandfix$setHints(int width, int height, String title, long monitor, GpuBackend backend, CallbackInfoReturnable<Long> callback) {
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUS_ON_SHOW, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHintString(GLFW.GLFW_WAYLAND_APP_ID, "minecraft");
    }

    @Inject(method = "setMode", at = @At("RETURN"))
    private void waylandfix$reconcileWaylandSizes(CallbackInfo callback) {
        if (GLFW.glfwGetPlatform() != GLFW.GLFW_PLATFORM_WAYLAND) {
            return;
        }

        int[] windowWidth = new int[1];
        int[] windowHeight = new int[1];
        GLFW.glfwGetWindowSize(handle, windowWidth, windowHeight);
        if (windowWidth[0] > 0 && windowHeight[0] > 0) {
            setWidth(windowWidth[0]);
            setHeight(windowHeight[0]);
        }

        int[] framebufferWidth = new int[1];
        int[] framebufferHeight = new int[1];
        GLFW.glfwGetFramebufferSize(handle, framebufferWidth, framebufferHeight);
        if (framebufferWidth[0] > 0 && framebufferHeight[0] > 0) {
            this.framebufferWidth = framebufferWidth[0];
            this.framebufferHeight = framebufferHeight[0];
        }
    }

    @Inject(method = "onFocus", at = @At("TAIL"))
    private void waylandfix$releaseKeysOnFocusLoss(long callbackWindow, boolean focused, CallbackInfo callback) {
        if (!focused && callbackWindow == handle
                && GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
            // Wayland compositors may cancel a pointer-lock transition without
            // delivering releases for keys held during the transition.
            KeyMapping.releaseAll();
        }
    }

    @Inject(method = "setIcon", at = @At("HEAD"), cancellable = false)
    private void waylandfix$keepIconPath(PackResources resources, com.mojang.blaze3d.platform.IconSet iconSet, CallbackInfo callback) {
        // Native GLFW 3.4.1 accepts the normal Minecraft icon path. This hook is
        // intentionally observational until xdg_toplevel_icon support is selected.
    }
}
