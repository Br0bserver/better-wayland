package dev.br0b.betterwayland.mixin;

import com.mojang.blaze3d.platform.Window;
//#if MC >= 260100
import com.mojang.blaze3d.systems.GpuBackend;
//#endif
import net.minecraft.client.KeyMapping;
import net.minecraft.server.packs.PackResources;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#if MC >= 260100
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#endif

@Mixin(Window.class)
public abstract class WindowMixin {
    @Unique
    private static final int betterwayland$waylandAppIdHint = 0x00026001;

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

    //#if MC >= 260100
    @Inject(method = "createGlfwWindow", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuBackend;setWindowHints()V", shift = At.Shift.AFTER, remap = false))
    private static void betterwayland$setHints(int width, int height, String title, long monitor, GpuBackend backend, CallbackInfoReturnable<Long> callback) {
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUS_ON_SHOW, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHintString(betterwayland$waylandAppIdHint, "minecraft");
    }
    //#else
    //$$ @Inject(
    //$$         method = "<init>",
    //$$         at = @At(
    //$$                 value = "INVOKE",
    //$$                 target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J",
    //$$                 remap = false))
    //$$ private void betterwayland$setHints(CallbackInfo callback) {
    //$$     GLFW.glfwWindowHint(GLFW.GLFW_FOCUS_ON_SHOW, GLFW.GLFW_FALSE);
    //$$     GLFW.glfwWindowHintString(betterwayland$waylandAppIdHint, "minecraft");
    //$$ }
    //#endif

    @Inject(method = "setMode", at = @At("RETURN"))
    private void betterwayland$reconcileWaylandSizes(CallbackInfo callback) {
        if (GLFW.glfwGetPlatform() != GLFW.GLFW_PLATFORM_WAYLAND) {
            return;
        }

        int[] windowWidth = new int[1];
        int[] windowHeight = new int[1];
        GLFW.glfwGetWindowSize(this.handle, windowWidth, windowHeight);
        if (windowWidth[0] > 0 && windowHeight[0] > 0) {
            setWidth(windowWidth[0]);
            setHeight(windowHeight[0]);
        }

        int[] framebufferWidth = new int[1];
        int[] framebufferHeight = new int[1];
        GLFW.glfwGetFramebufferSize(this.handle, framebufferWidth, framebufferHeight);
        if (framebufferWidth[0] > 0 && framebufferHeight[0] > 0) {
            this.framebufferWidth = framebufferWidth[0];
            this.framebufferHeight = framebufferHeight[0];
        }
    }

    @Inject(method = "onFocus", at = @At("TAIL"))
    private void betterwayland$releaseKeysOnFocusLoss(long callbackWindow, boolean focused, CallbackInfo callback) {
        if (!focused && callbackWindow == this.handle
                && GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
            // Wayland compositors may cancel a pointer-lock transition without
            // delivering releases for keys held during the transition.
            KeyMapping.releaseAll();
        }
    }

    @Inject(method = "setIcon", at = @At("HEAD"), cancellable = false)
    private void betterwayland$keepIconPath(PackResources resources, com.mojang.blaze3d.platform.IconSet iconSet, CallbackInfo callback) {
        // Native GLFW 3.4.1 accepts the normal Minecraft icon path. This hook is
        // intentionally observational until xdg_toplevel_icon support is selected.
    }
}
