//#if MC < 260100
package dev.br0b.betterwayland.mixin;

import dev.br0b.betterwayland.input.LegacyImeController;
import dev.br0b.betterwayland.input.LegacyTextTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(EditBox.class)
public abstract class LegacyEditBoxMixin implements LegacyTextTarget {
    @Shadow
    private String suggestion;

    @Unique
    private boolean betterwayland$preeditVisible;

    @Unique
    private int betterwayland$preeditCaretOffset;

    @Inject(method = "setFocused", at = @At("TAIL"))
    private void betterwayland$updateImeFocus(boolean focused, CallbackInfo callback) {
        LegacyImeController.focus(this, focused);
    }

    @Override
    public String betterwayland$getSuggestion() {
        return suggestion;
    }

    @Override
    public void betterwayland$setPreedit(String preedit, int caret, String fallbackSuggestion) {
        betterwayland$preeditVisible = preedit != null;
        betterwayland$preeditCaretOffset = betterwayland$preeditVisible
                ? betterwayland$measurePreeditCaret(preedit, caret)
                : 0;
        ((EditBox) (Object) this).setSuggestion(
                betterwayland$preeditVisible ? preedit : fallbackSuggestion);
    }

    @Unique
    private static int betterwayland$measurePreeditCaret(String preedit, int caret) {
        int codePoints = preedit.codePointCount(0, preedit.length());
        int safeCaret = caret <= 0 && codePoints > 0
                ? codePoints
                : Math.min(caret, codePoints);
        int charIndex = preedit.offsetByCodePoints(0, safeCaret);
        return Minecraft.getInstance().font.width(preedit.substring(0, charIndex));
    }

    @ModifyConstant(
            method = "renderWidget",
            constant = @Constant(intValue = 0xFF808080),
            require = 0)
    private int betterwayland$brightenPreedit(int color) {
        return betterwayland$preeditVisible ? 0xFFFFFFFF : color;
    }

    @ModifyArg(
            method = "renderWidget",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I",
                    ordinal = 1),
            index = 2,
            require = 0)
    private int betterwayland$movePreeditCursor(int x) {
        return betterwayland$preeditVisible ? x + betterwayland$preeditCaretOffset : x;
    }

    @ModifyArgs(
            method = "renderWidget",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;fill(Lnet/minecraft/client/renderer/RenderType;IIIII)V",
                    ordinal = 0),
            require = 0)
    private void betterwayland$moveVerticalPreeditCursor(Args args) {
        if (betterwayland$preeditVisible) {
            args.set(1, (int) args.get(1) + betterwayland$preeditCaretOffset);
            args.set(3, (int) args.get(3) + betterwayland$preeditCaretOffset);
        }
    }

    @Override
    public int[] betterwayland$getCandidateRectangle() {
        EditBox editBox = (EditBox) (Object) this;
        return new int[] {
                editBox.getScreenX(editBox.getCursorPosition()),
                editBox.getY(),
                1,
                editBox.getHeight()
        };
    }
}
//#endif
