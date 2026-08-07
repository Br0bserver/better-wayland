//#if MC >= 260100
package dev.br0b.waylandfix.mixin;

import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SharedConstants.class)
public final class SharedConstantsMixin {
    @Inject(method = "debugFlag", at = @At("HEAD"), cancellable = true)
    private static void waylandfix$preferWayland(String name, CallbackInfoReturnable<Boolean> callback) {
        if ("PREFER_WAYLAND".equals(name)) {
            callback.setReturnValue(true);
        }
    }
}
//#endif
