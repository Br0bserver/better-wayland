//#if MC < 260100
package dev.br0b.betterwayland.mixin;

import dev.br0b.betterwayland.input.LegacyImeController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class LegacyMinecraftMixin {
    @Inject(method = "setScreen", at = @At("HEAD"))
    private void betterwayland$resetImeFocus(Screen screen, CallbackInfo callback) {
        LegacyImeController.clearFocus();
    }

    @Inject(method = "setScreen", at = @At("TAIL"))
    private void betterwayland$updateLegacyIme(Screen screen, CallbackInfo callback) {
        LegacyImeController.setActive(screen != null);
    }
}
//#endif
