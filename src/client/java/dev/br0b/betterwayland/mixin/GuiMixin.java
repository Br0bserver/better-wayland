//#if MC >= 260100
package dev.br0b.betterwayland.mixin;

import dev.br0b.betterwayland.input.TextInputFocusOwner;
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
    //$$ private void betterwayland$clearTextFocusOnScreenChange(Screen screen, CallbackInfo callback) {
    //$$     ((TextInputFocusOwner) minecraft).betterwayland$clearTextInputFocus();
    //$$ }
    //#endif
}
//#endif
