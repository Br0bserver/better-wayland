package dev.br0b.waylandfix.mixin;

import dev.br0b.waylandfix.input.InputSuppression;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public final class KeyboardHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private final InputSuppression waylandfix$input = new InputSuppression();

    @Inject(method = "keyPress", at = @At("HEAD"))
    private void waylandfix$observeKey(long window, int action, KeyEvent event, CallbackInfo callback) {
        if (minecraft.screen == null) {
            boolean opensTextScreen = minecraft.options.keyChat.matches(event)
                    || minecraft.options.keyCommand.matches(event);
            waylandfix$input.observeGameplayKey(event.key(), action, opensTextScreen);
        } else {
            waylandfix$input.clear();
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void waylandfix$dropOpeningCharacter(long window, CharacterEvent event, CallbackInfo callback) {
        if (waylandfix$input.consumeOpeningCharacter(event.codepoint())) {
            callback.cancel();
        }
    }
}
