package net.phoenix.chromatic_codes.mixin;

import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.phoenix.ChromaticAPI;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

        ci.cancel();

        // 1. Transform Engine (Scaling)
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

        // 2. The "Short Word" Fix: Localized X
        // Using a 160.0f window ensures the gradient repeats consistently
        float localizedX = x % 160.0f;

        int colorL = effect.getRenderColor(0, localizedX + this.left, y);
        int colorR = effect.getRenderColor(0, localizedX + this.right, y);

        // Convert to 0.0-1.0 float for OpenGL
        float rL = ((colorL >> 16) & 0xFF) / 255.0f;
        float gL = ((colorL >> 8) & 0xFF) / 255.0f;
        float bL = (colorL & 0xFF) / 255.0f;
        float rR = ((colorR >> 16) & 0xFF) / 255.0f;
        float gR = ((colorR >> 8) & 0xFF) / 255.0f;
        float bR = (colorR & 0xFF) / 255.0f;

        // 3. Italics & Render
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
}
