//#if MC >= 260100
package dev.br0b.betterwayland.mixin;

import com.mojang.blaze3d.platform.TextInputManager;
import com.mojang.blaze3d.platform.Window;
import dev.br0b.betterwayland.BetterWaylandClient;
import dev.br0b.betterwayland.input.WaylandTextInputCoordinates;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Makes Minecraft's text-input rectangle use Wayland logical surface coordinates. */
@Mixin(TextInputManager.class)
public abstract class TextInputManagerMixin {
    @Shadow
    @Final
    private Window window;

    private static boolean loggedScale;

    @Inject(method = "startTextInput", at = @At("TAIL"))
    private void betterwayland$enableNativeIme(CallbackInfo callbackInfo) {
        betterwayland$setNativeIme(true);
    }

    @Inject(method = "stopTextInput", at = @At("TAIL"))
    private void betterwayland$disableNativeIme(CallbackInfo callbackInfo) {
        betterwayland$setNativeIme(false);
    }

    @Unique
    private void betterwayland$setNativeIme(boolean active) {
        if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
            GLFW.glfwSetInputMode(
                    window.handle(),
                    GLFW.GLFW_IME,
                    active ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        }
    }

    @Inject(method = "setTextInputArea", at = @At("HEAD"), cancellable = true)
    private void betterwayland$setLogicalTextInputArea(
            int left, int top, int right, int bottom, CallbackInfo callbackInfo) {
        if (GLFW.glfwGetPlatform() != GLFW.GLFW_PLATFORM_WAYLAND) {
            return;
        }

        long handle = window.handle();
        int[] windowWidth = new int[1];
        int[] windowHeight = new int[1];
        int[] framebufferWidth = new int[1];
        int[] framebufferHeight = new int[1];
        GLFW.glfwGetWindowSize(handle, windowWidth, windowHeight);
        GLFW.glfwGetFramebufferSize(handle, framebufferWidth, framebufferHeight);
        if (windowWidth[0] <= 0 || windowHeight[0] <= 0
                || framebufferWidth[0] <= 0 || framebufferHeight[0] <= 0) {
            return;
        }

        int guiScale = window.getGuiScale();
        int x = WaylandTextInputCoordinates.position(left, guiScale, windowWidth[0], framebufferWidth[0]);
        int y = WaylandTextInputCoordinates.position(top, guiScale, windowHeight[0], framebufferHeight[0]);
        int width = WaylandTextInputCoordinates.extent(right - left, guiScale, windowWidth[0], framebufferWidth[0]);
        int height = WaylandTextInputCoordinates.extent(bottom - top, guiScale, windowHeight[0], framebufferHeight[0]);

        if (!loggedScale) {
            loggedScale = true;
            BetterWaylandClient.LOGGER.info(
                    "Using logical Wayland text-input coordinates: window={}x{}, framebuffer={}x{}, guiScale={}",
                    windowWidth[0], windowHeight[0], framebufferWidth[0], framebufferHeight[0], guiScale);
        }

        GLFW.glfwSetPreeditCursorRectangle(handle, x, y, width, height);
        callbackInfo.cancel();
    }
}
//#endif
