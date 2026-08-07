//#if MC < 260100
package dev.br0b.betterwayland.mixin;

import dev.br0b.betterwayland.input.LegacyImeController;
import dev.br0b.betterwayland.input.LegacyTextTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Pseudo
@Mixin(targets = "me.shedaniel.rei.impl.client.gui.widget.basewidgets.TextFieldWidget", remap = false)
public abstract class LegacyReiTextFieldMixin implements LegacyTextTarget {
    @Shadow
    private String suggestion;

    @Unique
    private String betterwayland$preedit;

    @Unique
    private int betterwayland$preeditCaretOffset;

    @Unique
    private int betterwayland$preeditX;

    @Unique
    private int betterwayland$preeditY;

    @Unique
    private boolean betterwayland$preeditPositionValid;

    @Unique
    private Method betterwayland$getBoundsMethod;

    @Unique
    private Field betterwayland$boundsY;

    @Unique
    private Field betterwayland$boundsHeight;

    @Shadow
    public abstract int getCursor();

    @Shadow
    public abstract int getCharacterX(int index);

    @Shadow
    public abstract boolean hasBorder();

    @Inject(method = "method_25365(Z)V", at = @At("TAIL"), require = 0, remap = false)
    private void betterwayland$updateImeFocus(boolean focused, CallbackInfo callback) {
        LegacyImeController.focus(this, focused);
    }

    @Override
    public String betterwayland$getSuggestion() {
        return suggestion;
    }

    @Override
    public void betterwayland$setPreedit(String preedit, int caret, String fallbackSuggestion) {
        betterwayland$preedit = preedit;
        betterwayland$preeditCaretOffset = preedit == null
                ? 0
                : betterwayland$measurePreeditCaret(preedit, caret);
        betterwayland$preeditPositionValid = false;

        int[] rectangle = preedit == null ? null : betterwayland$readCandidateRectangle();
        if (rectangle != null) {
            betterwayland$preeditX = rectangle[0];
            betterwayland$preeditY = rectangle[1] + Math.max(0, (rectangle[3] - 8) / 2);
            betterwayland$preeditPositionValid = true;
        }
    }

    @Inject(method = "method_25394", at = @At("TAIL"), require = 0, remap = false)
    private void betterwayland$renderPreedit(
            GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {
        if (betterwayland$preedit == null
                || betterwayland$preedit.isEmpty()
                || !betterwayland$preeditPositionValid) {
            return;
        }

        graphics.drawString(
                Minecraft.getInstance().font,
                betterwayland$preedit,
                betterwayland$preeditX,
                betterwayland$preeditY,
                0xFFFFFFFF,
                false);
    }

    @ModifyArg(
            method = "method_25394",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/class_332;method_25303(Lnet/minecraft/class_327;Ljava/lang/String;III)I",
                    ordinal = 0,
                    remap = false),
            index = 2,
            require = 0,
            remap = false)
    private int betterwayland$movePreeditCursor(int x) {
        return betterwayland$preedit == null ? x : x + betterwayland$preeditCaretOffset;
    }

    @ModifyArgs(
            method = "method_25394",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/class_332;method_51739(Lnet/minecraft/class_1921;IIIII)V",
                    ordinal = 0,
                    remap = false),
            require = 0,
            remap = false)
    private void betterwayland$moveVerticalPreeditCursor(Args args) {
        if (betterwayland$preedit != null) {
            args.set(1, (int) args.get(1) + betterwayland$preeditCaretOffset);
            args.set(3, (int) args.get(3) + betterwayland$preeditCaretOffset);
        }
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

    @Override
    public int[] betterwayland$getCandidateRectangle() {
        return betterwayland$readCandidateRectangle();
    }

    @Unique
    private int[] betterwayland$readCandidateRectangle() {
        try {
            if (betterwayland$getBoundsMethod == null) {
                betterwayland$getBoundsMethod = getClass().getMethod("getBounds");
                Object bounds = betterwayland$getBoundsMethod.invoke(this);
                betterwayland$boundsY = bounds.getClass().getField("y");
                betterwayland$boundsHeight = bounds.getClass().getField("height");
            }

            Object bounds = betterwayland$getBoundsMethod.invoke(this);
            int textStartOffset = hasBorder() ? 4 : 0;
            return new int[] {
                    getCharacterX(getCursor()) + textStartOffset,
                    betterwayland$boundsY.getInt(bounds),
                    1,
                    betterwayland$boundsHeight.getInt(bounds)
            };
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
//#endif
