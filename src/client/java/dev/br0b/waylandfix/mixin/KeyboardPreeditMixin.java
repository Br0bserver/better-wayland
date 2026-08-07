//#if MC >= 260100
package dev.br0b.waylandfix.mixin;

import dev.br0b.waylandfix.input.PreeditKeyIsolation;
import dev.br0b.waylandfix.input.PreeditResettable;
import dev.br0b.waylandfix.input.TextInputFocusOwner;
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
    private final PreeditKeyIsolation waylandfix$preeditKeys = new PreeditKeyIsolation();

    @Unique
    private Object waylandfix$lastScreen;

    @Unique
    private Object waylandfix$lastFocus;

    @Unique
    private Screen waylandfix$currentScreen() {
        //#if MC >= 260200
        //$$ return minecraft.gui.screen();
        //#else
        return minecraft.screen;
        //#endif
    }

    @Override
    public void waylandfix$resetPreedit() {
        lastPreeditEvent = null;
        waylandfix$preeditKeys.reset();
    }

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true, order = 900)
    private void waylandfix$isolatePreeditKeys(long window, int action, KeyEvent event, CallbackInfo callback) {
        Screen currentScreen = waylandfix$currentScreen();
        Object currentFocus = currentScreen == null ? null : currentScreen.getFocused();
        if (currentScreen != waylandfix$lastScreen || currentFocus != waylandfix$lastFocus) {
            waylandfix$preeditKeys.reset();
            waylandfix$lastScreen = currentScreen;
            waylandfix$lastFocus = currentFocus;
        }

        boolean isWaylandWindow = window == minecraft.getWindow().handle()
                && GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND;
        if (isWaylandWindow && currentScreen == null) {
            waylandfix$preeditKeys.observePreedit(null);
        }
        if (isWaylandWindow
                && waylandfix$preeditKeys.shouldSuppress(event.key(), action, event.modifiers())) {
            callback.cancel();
        }
    }

    @Inject(method = "preeditCallback", at = @At("HEAD"), cancellable = true)
    private void waylandfix$trackPreedit(long window, PreeditEvent event, CallbackInfo callback) {
        if (window == minecraft.getWindow().handle()
                && GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
            waylandfix$preeditKeys.observePreedit(event == null ? null : event.fullText());
            GuiEventListener focusOwner = ((TextInputFocusOwner) minecraft).waylandfix$getTextInputFocusOwner();
            if (focusOwner != null) {
                lastPreeditEvent = event;
                KeyboardHandler.submitPreeditEvent(focusOwner, event);
                callback.cancel();
            }
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), order = 800)
    private void waylandfix$observeCharacter(long window, CharacterEvent event, CallbackInfo callback) {
        waylandfix$preeditKeys.observeCharacter();
    }
}
//#endif
