package dev.br0b.waylandfix.mixin;

import dev.br0b.waylandfix.input.PreeditResettable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public final class MinecraftMixin {
    @Inject(method = "<init>", at = @At("HEAD"))
    private static void waylandfix$setPlatformHint(CallbackInfo callback) {
        GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_WAYLAND);
    }

    @Inject(method = "onTextInputFocusChange", at = @At("HEAD"))
    private void waylandfix$resetNativePreedit(
            GuiEventListener listener,
            boolean focused,
            CallbackInfo callback) {
        if (!focused && GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
            Minecraft minecraft = (Minecraft) (Object) this;
            ((PreeditResettable) minecraft.keyboardHandler).waylandfix$resetPreedit();
            GLFW.glfwResetPreeditText(minecraft.getWindow().handle());
        }
    }
}
