//#if MC >= 260100
package dev.br0b.betterwayland.mixin;

import dev.br0b.betterwayland.input.PreeditKeyIsolation;
import dev.br0b.betterwayland.input.PreeditResettable;
import dev.br0b.betterwayland.input.TextInputFocusOwner;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.PreeditEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public final class KeyboardPreeditMixin implements PreeditResettable {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private PreeditEvent lastPreeditEvent;

    @Unique
    private final PreeditKeyIsolation betterwayland$preeditKeys = new PreeditKeyIsolation();

    @Unique
    private Object betterwayland$lastScreen;

    @Unique
    private Object betterwayland$lastFocus;

    @Unique
    private Screen betterwayland$currentScreen() {
        //#if MC >= 260200
        //$$ return minecraft.gui.screen();
        //#else
        return minecraft.screen;
        //#endif
    }

    @Override
    public void betterwayland$resetPreedit() {
        lastPreeditEvent = null;
        betterwayland$preeditKeys.reset();
    }

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true, order = 900)
    private void betterwayland$isolatePreeditKeys(long window, int action, KeyEvent event, CallbackInfo callback) {
        Screen currentScreen = betterwayland$currentScreen();
        Object currentFocus = currentScreen == null ? null : currentScreen.getFocused();
        if (currentScreen != betterwayland$lastScreen || currentFocus != betterwayland$lastFocus) {
            betterwayland$preeditKeys.reset();
            betterwayland$lastScreen = currentScreen;
            betterwayland$lastFocus = currentFocus;
        }

        boolean isWaylandWindow = window == minecraft.getWindow().handle()
                && GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND;
        if (isWaylandWindow && currentScreen == null) {
            betterwayland$preeditKeys.observePreedit(null);
        }
        if (isWaylandWindow
                && betterwayland$preeditKeys.shouldSuppress(event.key(), action, event.modifiers())) {
            callback.cancel();
        }
    }

    @Inject(method = "preeditCallback", at = @At("HEAD"), cancellable = true)
    private void betterwayland$trackPreedit(long window, PreeditEvent event, CallbackInfo callback) {
        if (window == minecraft.getWindow().handle()
                && GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
            betterwayland$preeditKeys.observePreedit(event == null ? null : event.fullText());
            GuiEventListener focusOwner = ((TextInputFocusOwner) minecraft).betterwayland$getTextInputFocusOwner();
            if (focusOwner != null) {
                lastPreeditEvent = event;
                KeyboardHandler.submitPreeditEvent(focusOwner, event);
                callback.cancel();
            }
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), order = 800)
    private void betterwayland$observeCharacter(long window, CharacterEvent event, CallbackInfo callback) {
        betterwayland$preeditKeys.observeCharacter();
    }
}
//#endif
