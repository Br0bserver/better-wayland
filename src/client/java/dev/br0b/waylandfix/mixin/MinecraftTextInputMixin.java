//#if MC >= 260100
package dev.br0b.waylandfix.mixin;

import com.mojang.blaze3d.platform.TextInputManager;
import dev.br0b.waylandfix.input.InputResettable;
import dev.br0b.waylandfix.input.PreeditResettable;
import dev.br0b.waylandfix.input.TextInputFocusOwner;
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
    private GuiEventListener waylandfix$textFocusOwner;

    @Override
    public GuiEventListener waylandfix$getTextInputFocusOwner() {
        return waylandfix$textFocusOwner;
    }

    @Override
    public void waylandfix$clearTextInputFocus() {
        waylandfix$textFocusOwner = null;
        if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
            textInputManager.stopTextInput();
        }
    }

    //#if MC >= 260200
    //#else
    @Inject(method = "setScreen", at = @At("HEAD"))
    private void waylandfix$clearTextFocusOnScreenChange(Screen screen, CallbackInfo callback) {
        waylandfix$clearTextInputFocus();
    }
    //#endif

    @Inject(method = "onTextInputFocusChange", at = @At("HEAD"))
    private void waylandfix$resetNativePreedit(
            GuiEventListener listener,
            boolean focused,
            CallbackInfo callback) {
        boolean lostCurrentFocus = !focused && waylandfix$textFocusOwner == listener;
        if (focused) {
            waylandfix$textFocusOwner = listener;
        } else if (lostCurrentFocus) {
            waylandfix$textFocusOwner = null;
        }

        if (lostCurrentFocus && GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
            Minecraft minecraft = (Minecraft) (Object) this;
            ((InputResettable) minecraft.keyboardHandler).waylandfix$resetInput();
            ((PreeditResettable) minecraft.keyboardHandler).waylandfix$resetPreedit();
            GLFW.glfwResetPreeditText(minecraft.getWindow().handle());
        }
    }

    @Inject(method = "onTextInputFocusChange", at = @At("TAIL"))
    private void waylandfix$restoreCurrentTextFocus(
            GuiEventListener listener,
            boolean focused,
            CallbackInfo callback) {
        boolean shouldBeActive = waylandfix$textFocusOwner != null;
        if (shouldBeActive != focused) {
            textInputManager.onTextInputFocusChange(shouldBeActive);
        }
    }
}
//#endif
