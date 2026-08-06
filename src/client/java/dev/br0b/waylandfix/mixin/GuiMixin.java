package dev.br0b.waylandfix.mixin;

import dev.br0b.waylandfix.input.TextInputFocusOwner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public final class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    //#if MC >= 260200
    //$$ @Inject(method = "setScreen", at = @At("HEAD"))
    //$$ private void waylandfix$clearTextFocusOnScreenChange(Screen screen, CallbackInfo callback) {
    //$$     ((TextInputFocusOwner) minecraft).waylandfix$clearTextInputFocus();
    //$$ }
    //#endif
}
