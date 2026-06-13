package net.phoenix.chromatic_codes.mixin;

import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.IChromaticEffect;
import net.phoenix.chromatic_codes.api.effects.LetterSpacingEffect;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(FontSet.class)
public class MixinFontSet {

    @Inject(method = "getGlyphInfo", at = @At("RETURN"), cancellable = true)
    private void phoenix$scaleGlyphWidth(int character, boolean filterFishyGlyphs,
                                         CallbackInfoReturnable<GlyphInfo> cir) {
        ResourceLocation currentFont = ChromaticAPI.getCurrentFontContext();

        // Ensure we only touch fonts belonging to your custom chromatic engine
        if (currentFont != null && currentFont.getNamespace().equals("phoenix_chromatic_codes")) {
            IChromaticEffect effect = ChromaticAPI.getByFont(currentFont);
            if (effect != null) {
                GlyphInfo original = cir.getReturnValue();
                if (original == null) return;

                // Case 1: Custom Structural Letter Spacing Cushion Effect ([spacing])
                if (effect instanceof LetterSpacingEffect spacingEffect) {
                    float cushion = spacingEffect.getSpacingCushion();

                    cir.setReturnValue(new GlyphInfo() {

                        @Override
                        public float getAdvance() {
                            return original.getAdvance() + cushion;
                        }

                        @Override
                        public float getAdvance(boolean bold) {
                            return original.getAdvance(bold) + cushion;
                        }

                        @Override
                        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> provider) {
                            return original.bake(provider);
                        }
                    });
                    return;
                }

                // Case 2: Standard Uniform Sizing Scale Factor Effect ([size])
                float scale = effect.getScale(0, 0);
                if (scale != 1.0f && scale > 0.0f) {
                    cir.setReturnValue(new GlyphInfo() {

                        @Override
                        public float getAdvance() {
                            return original.getAdvance() * scale;
                        }

                        @Override
                        public float getAdvance(boolean bold) {
                            return original.getAdvance(bold) * scale;
                        }

                        @Override
                        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> provider) {
                            return original.bake(provider);
                        }
                    });
                }
            }
        }
    }
}
