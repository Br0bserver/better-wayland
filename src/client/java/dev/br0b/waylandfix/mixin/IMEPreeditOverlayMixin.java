package dev.br0b.waylandfix.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.IMEPreeditOverlay;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps the native candidate rectangle update but hides Minecraft's duplicate floating preedit box. */
@Mixin(IMEPreeditOverlay.class)
public abstract class IMEPreeditOverlayMixin {
    @Shadow
    @Final
    private Font font;

    @Shadow
    private int inputLeft;

    @Shadow
    private int inputTop;

    @Shadow
    @Final
    private Component preEditText;

    @Inject(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/TextInputManager;setTextInputArea(IIII)V",
                    shift = At.Shift.AFTER),
            cancellable = true)
    private void waylandfix$hideFloatingPreedit(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo callbackInfo) {
        graphics.text(font, preEditText.copy().withColor(0xFFFFFFFF), inputLeft, inputTop, 0xFFFFFFFF, false);
        callbackInfo.cancel();
    }
}
