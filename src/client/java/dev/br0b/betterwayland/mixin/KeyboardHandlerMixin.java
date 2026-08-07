package dev.br0b.betterwayland.mixin;

import dev.br0b.betterwayland.input.InputSuppression;
import dev.br0b.betterwayland.input.InputResettable;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
//#if MC >= 12110
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
//#endif
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public final class KeyboardHandlerMixin implements InputResettable {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private final InputSuppression betterwayland$input = new InputSuppression();

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
    public void betterwayland$resetInput() {
        betterwayland$input.clear();
    }

    //#if MC >= 12110
    @Inject(method = "keyPress", at = @At("HEAD"), order = 800)
    private void betterwayland$observeKey(long window, int action, KeyEvent event, CallbackInfo callback) {
        boolean opensTextScreen = minecraft.options.keyChat.matches(event)
                || minecraft.options.keyCommand.matches(event);
        betterwayland$observeKeyState(event.key(), action, opensTextScreen);
    }
    //#else
    //$$ @Inject(method = "keyPress", at = @At("HEAD"), order = 800)
    //$$ private void betterwayland$observeKey(
    //$$         long window, int key, int scanCode, int action, int modifiers, CallbackInfo callback) {
    //$$     boolean opensTextScreen = minecraft.options.keyChat.matches(key, scanCode)
    //$$             || minecraft.options.keyCommand.matches(key, scanCode);
    //$$     betterwayland$observeKeyState(key, action, opensTextScreen);
    //$$ }
    //#endif

    @Unique
    private void betterwayland$observeKeyState(int key, int action, boolean opensTextScreen) {
        Screen currentScreen = betterwayland$currentScreen();
        Object currentFocus = currentScreen == null ? null : currentScreen.getFocused();
        if (currentScreen != betterwayland$lastScreen || currentFocus != betterwayland$lastFocus) {
            betterwayland$input.clear();
            betterwayland$lastScreen = currentScreen;
            betterwayland$lastFocus = currentFocus;
        }

        if (currentScreen == null) {
            betterwayland$input.observeGameplayKey(key, action, opensTextScreen);
        } else {
            betterwayland$input.clear();
        }
    }

    //#if MC >= 12110
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true, order = 900)
    private void betterwayland$dropOpeningCharacter(long window, CharacterEvent event, CallbackInfo callback) {
        if (betterwayland$input.consumeOpeningCharacter(event.codepoint())) {
            callback.cancel();
        }
    }
    //#else
    //$$ @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true, order = 900)
    //$$ private void betterwayland$dropOpeningCharacter(
    //$$         long window, int codePoint, int modifiers, CallbackInfo callback) {
    //$$     if (betterwayland$input.consumeOpeningCharacter(codePoint)) {
    //$$         callback.cancel();
    //$$     }
    //$$ }
    //#endif
}
