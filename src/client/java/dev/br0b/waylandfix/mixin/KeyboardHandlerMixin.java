package dev.br0b.waylandfix.mixin;

import dev.br0b.waylandfix.input.InputSuppression;
import dev.br0b.waylandfix.input.PreeditKeyIsolation;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
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
public final class KeyboardHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private final InputSuppression waylandfix$input = new InputSuppression();

    @Unique
    private final PreeditKeyIsolation waylandfix$preeditKeys = new PreeditKeyIsolation();

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void waylandfix$observeKey(long window, int action, KeyEvent event, CallbackInfo callback) {
        boolean isWaylandWindow = window == minecraft.getWindow().handle()
                && GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND;
        if (minecraft.screen == null) {
            boolean opensTextScreen = minecraft.options.keyChat.matches(event)
                    || minecraft.options.keyCommand.matches(event);
            waylandfix$input.observeGameplayKey(event.key(), action, opensTextScreen);
            if (isWaylandWindow) {
                waylandfix$preeditKeys.observePreedit(null);
            }
        } else {
            waylandfix$input.clear();
        }

        if (isWaylandWindow
                && waylandfix$preeditKeys.shouldSuppress(event.key(), action, event.modifiers())) {
            callback.cancel();
        }
    }

    @Inject(method = "preeditCallback", at = @At("HEAD"))
    private void waylandfix$trackPreedit(long window, PreeditEvent event, CallbackInfo callback) {
        if (window == minecraft.getWindow().handle()
                && GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
            waylandfix$preeditKeys.observePreedit(event.fullText());
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void waylandfix$dropOpeningCharacter(long window, CharacterEvent event, CallbackInfo callback) {
        waylandfix$preeditKeys.observeCharacter();
        if (waylandfix$input.consumeOpeningCharacter(event.codepoint())) {
            callback.cancel();
        }
    }
}
