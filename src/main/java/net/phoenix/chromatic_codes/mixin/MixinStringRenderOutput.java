package net.phoenix.chromatic_codes.mixin;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.phoenix.ChromaticAPI;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput", priority = 2000)
public abstract class MixinStringRenderOutput {

    @Shadow
    float x;
    @Shadow
    float y;

    @Inject(method = "accept", at = @At("HEAD"))
    private void phoenix$setupContext(int index, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        if (style != null && style.getFont() != null &&
                style.getFont().getNamespace().equals("phoenix_chromatic_codes")) {
            IChromaticEffect effect = ChromaticAPI.getByFont(style.getFont());
            ChromaticAPI.setCurrentEffect(effect);
        } else {
            ChromaticAPI.setCurrentEffect(null);
        }
    }

    @Inject(method = "accept", at = @At("RETURN"))
    private void phoenix$cleanupContext(int index, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        ChromaticAPI.setCurrentEffect(null);
    }

    @ModifyVariable(method = "accept", at = @At("HEAD"), argsOnly = true)
    private Style phoenix$applyApiEffects(Style style) {
        if (style == null || style.getFont() == null) return style;

        if (style.getFont().getNamespace().equals("phoenix_chromatic_codes")) {
            IChromaticEffect effect = ChromaticAPI.getByFont(style.getFont());
            if (effect != null) {
                return style.withColor(TextColor.fromRgb(effect.getRenderColor(0, this.x, this.y)));
            }
        }
        return style;
    }

    @Inject(method = "accept", at = @At("HEAD"))
    private void phoenix$applyAllTransforms(int index, Style style, int codePoint,
                                            CallbackInfoReturnable<Boolean> cir) {
        if (style == null || style.getFont() == null) return;

        IChromaticEffect effect = ChromaticAPI.getByFont(style.getFont());
        if (effect != null) {
            this.x += effect.getXOffset(this.x, this.y);
            this.y += effect.getYOffset(this.x, this.y);

            float scale = effect.getScale(this.x, this.y);
            if (scale != 1.0f) {
                this.x += (1.0f - scale) * 4.0f;
            }
        }
    }
}
