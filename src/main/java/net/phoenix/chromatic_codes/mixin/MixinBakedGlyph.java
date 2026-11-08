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

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
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
            phoenix$renderGradientGlyph(gradient, italic, x, y, matrix, buffer, alpha, packedLight,
                    shadowPass ? 0.25f : 1.0f);
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
        float finalYUp = centerY - (charHalfHeight * scaleY) - 3.0f;
        float finalYDown = centerY + (charHalfHeight * scaleY) - 3.0f;

        float localizedX = x % 160.0f;

        int colorL = effect.getRenderColor(0, localizedX + this.left, y);
        int colorR = effect.getRenderColor(0, localizedX + this.right, y);

        float rL = ((colorL >> 16) & 0xFF) / 255.0f;
        float gL = ((colorL >> 8) & 0xFF) / 255.0f;
        float bL = (colorL & 0xFF) / 255.0f;
        float rR = ((colorR >> 16) & 0xFF) / 255.0f;
        float gR = ((colorR >> 8) & 0xFF) / 255.0f;
        float bR = (colorR & 0xFF) / 255.0f;

        float italicTop = italic ? 1.0F - 0.25F * (this.up - 3.0f) : 0.0F;
        float italicBottom = italic ? 1.0F - 0.25F * (this.down - 3.0f) : 0.0F;

        buffer.vertex(matrix, finalXL + italicTop, finalYUp, 0.0F).color(rL, gL, bL, alpha).uv(this.u0, this.v0)
                .uv2(packedLight).endVertex();
        buffer.vertex(matrix, finalXL + italicBottom, finalYDown, 0.0F).color(rL, gL, bL, alpha).uv(this.u0, this.v1)
                .uv2(packedLight).endVertex();
        buffer.vertex(matrix, finalXR + italicBottom, finalYDown, 0.0F).color(rR, gR, bR, alpha).uv(this.u1, this.v1)
                .uv2(packedLight).endVertex();
        buffer.vertex(matrix, finalXR + italicTop, finalYUp, 0.0F).color(rR, gR, bR, alpha).uv(this.u1, this.v0)
                .uv2(packedLight).endVertex();
    }

    /**
     * Vanilla's quad, cut into a small grid so a multi-stop gradient comes out right (the GPU only interpolates
     * linearly between vertices). Same geometry/italic handling as vanilla's BakedGlyph.render; every vertex gets the
     * gradient color for its position in the glyph's em space. Baseline is 7px below the text's top, as for all
     * vanilla-positioned glyphs.
     */
    @Unique
    private void phoenix$renderGradientGlyph(GlyphGradient gradient, boolean italic, float x, float y, Matrix4f matrix,
                                             VertexConsumer buffer, float alpha, int packedLight, float shade) {
        final int n = 4;
        float top = this.up - 3.0f;
        float bottom = this.down - 3.0f;

        for (int row = 0; row < n; row++) {
            float t0 = row / (float) n;
            float t1 = (row + 1) / (float) n;
            for (int col = 0; col < n; col++) {
                float s0 = col / (float) n;
                float s1 = (col + 1) / (float) n;
                phoenix$gradientVertex(gradient, italic, x, y, matrix, buffer, alpha, packedLight, shade, s0, t0, top, bottom);
                phoenix$gradientVertex(gradient, italic, x, y, matrix, buffer, alpha, packedLight, shade, s0, t1, top, bottom);
                phoenix$gradientVertex(gradient, italic, x, y, matrix, buffer, alpha, packedLight, shade, s1, t1, top, bottom);
                phoenix$gradientVertex(gradient, italic, x, y, matrix, buffer, alpha, packedLight, shade, s1, t0, top, bottom);
            }
        }
    }

    @Unique
    private void phoenix$gradientVertex(GlyphGradient gradient, boolean italic, float x, float y, Matrix4f matrix,
                                        VertexConsumer buffer, float alpha, int packedLight, float shade, float s, float t,
                                        float top, float bottom) {
        float dx = this.left + (this.right - this.left) * s;     // px right of the pen
        float dy = top + (bottom - top) * t;                      // px below the text's top
        float shear = italic ? 1.0F - 0.25F * dy : 0.0F;
        int color = gradient.colorAt(dx, GlyphGradient.BASELINE - dy);
        buffer.vertex(matrix, x + dx + shear, y + dy, 0.0F)
                .color(((color >> 16) & 0xFF) / 255.0f * shade, ((color >> 8) & 0xFF) / 255.0f * shade, (color & 0xFF) / 255.0f * shade, alpha)
                .uv(this.u0 + (this.u1 - this.u0) * s, this.v0 + (this.v1 - this.v0) * t)
                .uv2(packedLight).endVertex();
    }
}
