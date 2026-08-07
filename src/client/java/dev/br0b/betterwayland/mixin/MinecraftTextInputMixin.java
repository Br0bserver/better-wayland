//#if MC >= 260100
package dev.br0b.betterwayland.mixin;

import com.mojang.blaze3d.platform.TextInputManager;
import dev.br0b.betterwayland.input.InputResettable;
import dev.br0b.betterwayland.input.PreeditResettable;
import dev.br0b.betterwayland.input.TextInputFocusOwner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public final class MinecraftTextInputMixin implements TextInputFocusOwner {
    @Shadow
    @Final
    private TextInputManager textInputManager;

    @Unique
    private GuiEventListener betterwayland$textFocusOwner;

    @Override
    public GuiEventListener betterwayland$getTextInputFocusOwner() {
        return betterwayland$textFocusOwner;
    }

    @Override
    public void betterwayland$clearTextInputFocus() {
        betterwayland$textFocusOwner = null;
        if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
            textInputManager.stopTextInput();
        }
    }

    //#if MC >= 260200
    //#else
    @Inject(method = "setScreen", at = @At("HEAD"))
    private void betterwayland$clearTextFocusOnScreenChange(Screen screen, CallbackInfo callback) {
        betterwayland$clearTextInputFocus();
    }
    //#endif

    @Inject(method = "onTextInputFocusChange", at = @At("HEAD"))
    private void betterwayland$resetNativePreedit(
            GuiEventListener listener,
            boolean focused,
            CallbackInfo callback) {
        boolean lostCurrentFocus = !focused && betterwayland$textFocusOwner == listener;
        if (focused) {
            betterwayland$textFocusOwner = listener;
        } else if (lostCurrentFocus) {
            betterwayland$textFocusOwner = null;
        }

        if (lostCurrentFocus && GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
            Minecraft minecraft = (Minecraft) (Object) this;
            ((InputResettable) minecraft.keyboardHandler).betterwayland$resetInput();
            ((PreeditResettable) minecraft.keyboardHandler).betterwayland$resetPreedit();
            GLFW.glfwResetPreeditText(minecraft.getWindow().handle());
        }
    }

    @Inject(method = "onTextInputFocusChange", at = @At("TAIL"))
    private void betterwayland$restoreCurrentTextFocus(
            GuiEventListener listener,
            boolean focused,
            CallbackInfo callback) {
        boolean shouldBeActive = betterwayland$textFocusOwner != null;
        if (shouldBeActive != focused) {
            textInputManager.onTextInputFocusChange(shouldBeActive);
        }
    }
}
//#endif
