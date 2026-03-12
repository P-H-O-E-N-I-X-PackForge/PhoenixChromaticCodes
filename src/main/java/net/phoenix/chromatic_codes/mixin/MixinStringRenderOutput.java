package net.phoenix.chromatic_codes.mixin;

import net.minecraft.network.chat.Style;
import net.phoenix.ChromaticAPI;
import net.phoenix.chromatic_codes.api.ChromaticEffects;
import net.phoenix.chromatic_codes.api.IChromaticEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public abstract class MixinStringRenderOutput {
    @Shadow float x;
    @Shadow float y;

    @ModifyVariable(method = "accept", at = @At("HEAD"), argsOnly = true)
    private Style phoenix$applyApiEffects(Style style) {
        IChromaticEffect effect = ChromaticAPI.getByFont(style.getFont());
        if (effect != null) {
            return effect.apply(style, this.x, this.y);
        }
        return style;
    }
}