package net.phoenix.chromatic_codes.mixin;

import net.minecraft.network.chat.Style;
import net.phoenix.ChromaticAPI;
import net.phoenix.chromatic_codes.api.IChromaticEffect;
import net.phoenix.chromatic_codes.api.RenderContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput", priority = 2000)
public abstract class MixinStringRenderOutput {

    @Shadow float x;
    @Shadow float y;

    @Unique
    private float phoenixChromaticCodes$offsetX;
    @Unique
    private float phoenixChromaticCodes$offsetY;

    @Inject(method = "accept", at = @At("HEAD"))
    private void phoenix$setup(int index, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {

        // Only apply effect once per render cycle
        if (!RenderContext.enter()) return;

        phoenixChromaticCodes$offsetX = 0;
        phoenixChromaticCodes$offsetY = 0;

        if (style != null && style.getFont() != null) {
            IChromaticEffect effect = ChromaticAPI.getByFont(style.getFont());
            ChromaticAPI.setCurrentEffect(effect);

            if (effect != null) {
                phoenixChromaticCodes$offsetX = effect.getXOffset(x, y);
                phoenixChromaticCodes$offsetY = effect.getYOffset(x, y);

                x += phoenixChromaticCodes$offsetX;
                y += phoenixChromaticCodes$offsetY;
            }
        }
    }

    @Inject(method = "accept", at = @At("RETURN"))
    private void phoenix$cleanup(int index, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {

        if (phoenixChromaticCodes$offsetX != 0 || phoenixChromaticCodes$offsetY != 0) {
            x -= phoenixChromaticCodes$offsetX;
            y -= phoenixChromaticCodes$offsetY;
        }

        ChromaticAPI.setCurrentEffect(null);
        RenderContext.exit();
    }
}