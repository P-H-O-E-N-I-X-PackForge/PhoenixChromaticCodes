package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.GlyphGradient;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

/**
 * A movement/color effect layered over a passthrough transform effect (scale/spacing/slant/shear/compress - a pure
 * geometry change that carries no color of its own, {@link IChromaticEffect#isPassthrough()}). Color, movement and
 * every other behavioural flag come from {@code colorEffect}; geometric transforms (scale, offsets) combine both, so
 * e.g. "big" (scale) + "wave" (color and motion) gets scaled, colored, moving text instead of one replacing the
 * other.
 */
public final class TransformLayerEffect implements IChromaticEffect {

    private final IChromaticEffect colorEffect;
    private final IChromaticEffect transform;

    public TransformLayerEffect(IChromaticEffect colorEffect, IChromaticEffect transform) {
        this.colorEffect = colorEffect;
        this.transform = transform;
    }

    public IChromaticEffect colorEffect() {
        return colorEffect;
    }

    public IChromaticEffect transform() {
        return transform;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        return colorEffect.getRenderColor(originalColor, x, y);
    }

    // If colorEffect is itself another passthrough transform (two transforms stacked with no color-providing side),
    // stay passthrough so the style's real color still shows through.
    @Override
    public boolean isPassthrough() {
        return colorEffect.isPassthrough();
    }

    @Override
    public GlyphGradient getGlyphGradient() {
        return colorEffect.getGlyphGradient();
    }

    @Override
    public float getScale(float x, float y) {
        return colorEffect.getScale(x, y) * transform.getScale(x, y);
    }

    @Override
    public float getScaleX(float x, float y) {
        return colorEffect.getScaleX(x, y) * transform.getScaleX(x, y);
    }

    @Override
    public float getScaleY(float x, float y) {
        return colorEffect.getScaleY(x, y) * transform.getScaleY(x, y);
    }

    @Override
    public float getXOffset(float x, float y) {
        return colorEffect.getXOffset(x, y) + transform.getXOffset(x, y);
    }

    @Override
    public float getYOffset(float x, float y) {
        return colorEffect.getYOffset(x, y) + transform.getYOffset(x, y);
    }

    @Override
    public boolean useHorizontalBlending() {
        return colorEffect.useHorizontalBlending();
    }

    @Override
    public boolean isOutline() {
        return colorEffect.isOutline() || transform.isOutline();
    }

    @Override
    public int getOutlineColor() {
        return colorEffect.isOutline() ? colorEffect.getOutlineColor() : transform.getOutlineColor();
    }

    @Override
    public boolean isVolumetric() {
        return colorEffect.isVolumetric() || transform.isVolumetric();
    }

    @Override
    public boolean isStatic() {
        return colorEffect.isStatic();
    }

    @Override
    public boolean isAnimated() {
        return colorEffect.isAnimated() || transform.isAnimated();
    }
}
