package net.phoenix.chromatic_codes.mixin;

import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.GlyphGradient;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BakedGlyph.class)
public abstract class MixinBakedGlyph {

    @Shadow
    @Final
    private float left;
    @Shadow
    @Final
    private float right;
    @Shadow
    @Final
    private float up;
    @Shadow
    @Final
    private float down;
    @Shadow
    @Final
    private float u0;
    @Shadow
    @Final
    private float u1;
    @Shadow
    @Final
    private float v0;
    @Shadow
    @Final
    private float v1;

    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void phoenix$renderTrueGradient(boolean italic, float x, float y, Matrix4f matrix, VertexConsumer buffer,
                                            float red, float green, float blue, float alpha, int packedLight,
                                            CallbackInfo ci) {
        IChromaticEffect effect = ChromaticAPI.getCurrentEffect();
        if (effect == null) return;

        // Gradient glyphs draw their own shadow too: vanilla's shadow pass arrives as a dark color (25% of the forced
        // white text color), which would be a flat gray ghost behind the gradient. Shade the gradient instead.
        GlyphGradient gradient = effect.getGlyphGradient();
        if (gradient != null) {
            boolean shadowPass = red < 0.3f && green < 0.3f && blue < 0.3f;
            ci.cancel();
            // Movement over a gradient font (GradientLayerEffect): same scaling rules as the effect path below
            float gScaleX = effect.getScaleX(x, y);
            float gScaleY = effect.getScaleY(x, y);
            float gUniform = effect.getScale(x, y);
            if (gUniform != 1.0f) {
                gScaleX = gUniform;
                gScaleY = gUniform;
            }
            phoenix$renderGradientGlyph(gradient, italic, x, y, matrix, buffer, alpha, packedLight,
                    shadowPass ? 0.25f : 1.0f, gScaleX, gScaleY);
            return;
        }

        if (red < 0.3f && green < 0.3f && blue < 0.3f) return;

        ci.cancel();

        float scaleX = effect.getScaleX(x, y);
        float scaleY = effect.getScaleY(x, y);
        float uniformScale = effect.getScale(x, y);
        if (uniformScale != 1.0f) {
            scaleX = uniformScale;
            scaleY = uniformScale;
        }

        float centerX = x + (this.left + this.right) / 2.0f;
        float centerY = y + (this.up + this.down) / 2.0f;
        float charHalfWidth = (this.right - this.left) / 2.0f;
        float charHalfHeight = (this.down - this.up) / 2.0f;

        float finalXL = centerX - (charHalfWidth * scaleX);
        float finalXR = centerX + (charHalfWidth * scaleX);
        float finalYUp = centerY - (charHalfHeight * scaleY);
        float finalYDown = centerY + (charHalfHeight * scaleY);

        float localizedX = x % 160.0f;

        int colorL = effect.getRenderColor(0, localizedX + this.left, y);
        int colorR = effect.getRenderColor(0, localizedX + this.right, y);

        float rL = ((colorL >> 16) & 0xFF) / 255.0f;
        float gL = ((colorL >> 8) & 0xFF) / 255.0f;
        float bL = (colorL & 0xFF) / 255.0f;
        float rR = ((colorR >> 16) & 0xFF) / 255.0f;
        float gR = ((colorR >> 8) & 0xFF) / 255.0f;
        float bR = (colorR & 0xFF) / 255.0f;

        float italicTop = italic ? 1.0F - 0.25F * this.up : 0.0F;
        float italicBottom = italic ? 1.0F - 0.25F * this.down : 0.0F;

        buffer.addVertex(matrix, finalXL + italicTop, finalYUp, 0.0F)
                .setColor(rL, gL, bL, alpha)
                .setUv(this.u0, this.v0)
                .setLight(packedLight);

        buffer.addVertex(matrix, finalXL + italicBottom, finalYDown, 0.0F)
                .setColor(rL, gL, bL, alpha)
                .setUv(this.u0, this.v1)
                .setLight(packedLight);

        buffer.addVertex(matrix, finalXR + italicBottom, finalYDown, 0.0F)
                .setColor(rR, gR, bR, alpha)
                .setUv(this.u1, this.v1)
                .setLight(packedLight);

        buffer.addVertex(matrix, finalXR + italicTop, finalYUp, 0.0F)
                .setColor(rR, gR, bR, alpha)
                .setUv(this.u1, this.v0)
                .setLight(packedLight);
    }

    /**
     * Vanilla's quad, cut into a small grid so a multi-stop gradient comes out right (the GPU only interpolates
     * linearly between vertices). Same geometry/italic handling as the effect path above; every vertex gets the
     * gradient color for its position in the glyph's em space. Baseline is 7px below the text's top, as for all
     * vanilla-positioned glyphs.
     */
    @Unique
    private void phoenix$renderGradientGlyph(GlyphGradient gradient, boolean italic, float x, float y, Matrix4f matrix,
                                             VertexConsumer buffer, float alpha, int packedLight, float shade,
                                             float scaleX,
                                             float scaleY) {
        final int n = 4;

        for (int row = 0; row < n; row++) {
            float t0 = row / (float) n;
            float t1 = (row + 1) / (float) n;
            for (int col = 0; col < n; col++) {
                float s0 = col / (float) n;
                float s1 = (col + 1) / (float) n;
                phoenix$gradientVertex(gradient, italic, x, y, matrix, buffer, alpha, packedLight, shade, scaleX,
                        scaleY, s0, t0);
                phoenix$gradientVertex(gradient, italic, x, y, matrix, buffer, alpha, packedLight, shade, scaleX,
                        scaleY, s0, t1);
                phoenix$gradientVertex(gradient, italic, x, y, matrix, buffer, alpha, packedLight, shade, scaleX,
                        scaleY, s1, t1);
                phoenix$gradientVertex(gradient, italic, x, y, matrix, buffer, alpha, packedLight, shade, scaleX,
                        scaleY, s1, t0);
            }
        }
    }

    @Unique
    private void phoenix$gradientVertex(GlyphGradient gradient, boolean italic, float x, float y, Matrix4f matrix,
                                        VertexConsumer buffer, float alpha, int packedLight, float shade, float scaleX,
                                        float scaleY,
                                        float s, float t) {
        float dx = this.left + (this.right - this.left) * s;     // px right of the pen
        float dy = this.up + (this.down - this.up) * t;          // px below the text's top
        float shear = italic ? 1.0F - 0.25F * dy : 0.0F;
        int color = gradient.colorAt(dx, gradient.baseline() - dy);
        float midX = (this.left + this.right) / 2.0f;
        float midY = (this.up + this.down) / 2.0f;
        float px = x + midX + (dx - midX) * scaleX + shear;   // scale about the glyph's center, like the effect path
        float py = y + midY + (dy - midY) * scaleY;
        buffer.addVertex(matrix, px, py, 0.0F)
                .setColor(((color >> 16) & 0xFF) / 255.0f * shade, ((color >> 8) & 0xFF) / 255.0f * shade,
                        (color & 0xFF) / 255.0f * shade, alpha)
                .setUv(this.u0 + (this.u1 - this.u0) * s, this.v0 + (this.v1 - this.v0) * t)
                .setLight(packedLight);
    }
}
