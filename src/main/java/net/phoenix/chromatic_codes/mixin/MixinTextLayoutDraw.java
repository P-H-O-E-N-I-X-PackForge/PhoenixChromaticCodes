package net.phoenix.chromatic_codes.mixin;

import net.minecraft.client.renderer.MultiBufferSource;
import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.IChromaticEffect;
import net.phoenix.chromatic_codes.api.IChromaticLayoutExtensions;

import icyllis.modernui.mc.text.TextLayout;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TextLayout.class, remap = false)
public abstract class MixinTextLayoutDraw {

    // Using the raw SRG/Intermediary method handles to bypass Interface/Default method mapping bugs
    @Unique
    private static final String VERTEX_TARGET = "Lcom/mojang/blaze3d/vertex/VertexConsumer;m_252986_(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;";

    @Unique
    private static final String COLOR_TARGET = "Lcom/mojang/blaze3d/vertex/VertexConsumer;m_6122_(IIII)Lcom/mojang/blaze3d/vertex/VertexConsumer;";

    @Unique
    private int phoenix$currentGlyphIndex = 0;
    @Unique
    private int phoenix$currentCornerIndex = 0;
    @Unique
    private float phoenix$cachedXOffset = 0f;
    @Unique
    private float phoenix$cachedYOffset = 0f;
    @Unique
    private float phoenix$lastVertexX = 0f;
    @Unique
    private float phoenix$lastVertexY = 0f;
    @Unique
    private IChromaticEffect phoenix$previousEffect = null;
    @Unique
    private boolean phoenix$pendingGlyphAdvance = false;

    @Unique
    private IChromaticEffect phoenix$effectForGlyph(int glyphIndex) {
        IChromaticEffect[] effects = ((IChromaticLayoutExtensions) this).phoenix$getGlyphEffects();
        return (effects != null && glyphIndex >= 0 && glyphIndex < effects.length) ? effects[glyphIndex] : null;
    }

    @Inject(method = "drawText", at = @At("HEAD"))
    private void phoenix$resetGlyphCounter(Matrix4f matrix, MultiBufferSource source, float x, float top, int r, int g,
                                           int b, int a, boolean isShadow, int preferredMode, boolean polygonOffset,
                                           float uniformScale, int bgColor, int packedLight,
                                           CallbackInfoReturnable<Float> cir) {
        this.phoenix$currentGlyphIndex = 0;
        this.phoenix$currentCornerIndex = 0;
        this.phoenix$cachedXOffset = 0f;
        this.phoenix$cachedYOffset = 0f;
        this.phoenix$previousEffect = null;
        this.phoenix$pendingGlyphAdvance = false;
    }

    // Capture X coordinate argument (index 1) right before it hits m_252986_
    @ModifyArg(method = "drawText", at = @At(value = "INVOKE", target = VERTEX_TARGET, remap = false), index = 1)
    private float phoenix$adjustX(float x) {
        if (phoenix$currentCornerIndex == 0) {
            if (phoenix$pendingGlyphAdvance) {
                phoenix$currentGlyphIndex++;
                phoenix$pendingGlyphAdvance = false;
            }

            IChromaticEffect effect = phoenix$effectForGlyph(phoenix$currentGlyphIndex);
            if (effect != phoenix$previousEffect) {
                ChromaticAPI.setSegmentStartX(x);
                phoenix$previousEffect = effect;
            }
            phoenix$cachedXOffset = (effect != null && !effect.isPassthrough()) ? effect.getXOffset(x, 0f) : 0f; // placeholder
                                                                                                                 // Y
                                                                                                                 // for
                                                                                                                 // calculation
                                                                                                                 // if
                                                                                                                 // needed
        }

        float finalX = x + phoenix$cachedXOffset;
        phoenix$lastVertexX = finalX;
        return finalX;
    }

    // Capture Y coordinate argument (index 2) right before it hits m_252986_
    @ModifyArg(method = "drawText", at = @At(value = "INVOKE", target = VERTEX_TARGET, remap = false), index = 2)
    private float phoenix$adjustY(float y) {
        if (phoenix$currentCornerIndex == 0) {
            IChromaticEffect effect = phoenix$effectForGlyph(phoenix$currentGlyphIndex);
            phoenix$cachedYOffset = (effect != null && !effect.isPassthrough()) ?
                    effect.getYOffset(phoenix$lastVertexX - phoenix$cachedXOffset, y) : 0f;
        }

        float finalY = y + phoenix$cachedYOffset;
        phoenix$lastVertexY = finalY;

        // Advance corner logic
        phoenix$currentCornerIndex = (phoenix$currentCornerIndex + 1) % 4;
        if (phoenix$currentCornerIndex == 0) {
            phoenix$pendingGlyphAdvance = true;
        }
        return finalY;
    }

    // Intercept and manipulate the color packed integers inside m_6122_
    @ModifyArg(method = "drawText", at = @At(value = "INVOKE", target = COLOR_TARGET, remap = false), index = 0)
    private int phoenix$adjustRed(int r) {
        // Since we are inside drawText, we can check shadow state via class variables or evaluate it safely.
        // To keep this clean, we pull the effect color transformation here:
        IChromaticEffect effect = phoenix$effectForGlyph(phoenix$currentGlyphIndex);
        if (effect != null) {
            // Note: If you need to ensure it's not a shadow, verify if your effects check can read the current text
            // alpha or pass a state variable.
            int original = ((r & 0xFF) << 16); // Extract basic layout values
            // Fallback recovery reconstruction logic or pass through depending on your needs:
            int rendered = effect.getRenderColor(original, phoenix$lastVertexX, phoenix$lastVertexY);
            return (rendered >> 16) & 0xFF;
        }
        return r;
    }
}
