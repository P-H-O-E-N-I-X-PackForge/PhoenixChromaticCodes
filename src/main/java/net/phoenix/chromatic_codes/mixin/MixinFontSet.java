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

    // Force the compiler to skip mapping verification via remap = false
    // and provide the explicit descriptor signature for getGlyphInfo(int, boolean)
    @Inject(
            method = "getGlyphInfo(IZ)Lcom/mojang/blaze3d/font/GlyphInfo;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false)
    private void phoenix$scaleGlyphWidth(int character, boolean filterFishyGlyphs,
                                         CallbackInfoReturnable<GlyphInfo> cir) {
        ResourceLocation currentFont = ChromaticAPI.getCurrentFontContext();

        if (currentFont != null && currentFont.getNamespace().equals("phoenix_chromatic_codes")) {
            IChromaticEffect effect = ChromaticAPI.getByFont(currentFont);
            if (effect != null) {
                GlyphInfo original = cir.getReturnValue();
                if (original == null) return;

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
