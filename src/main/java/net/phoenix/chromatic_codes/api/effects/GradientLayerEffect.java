package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.GlyphGradient;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

/**
 * A movement effect laid over a base font that ships its own gradient (phoenix_display_color): every motion property
 * (offsets, scaling, ...) comes from {@code movement}, every color from the font's {@link GlyphGradient}. The movement
 * effect's own colors are ignored.
 */
public final class GradientLayerEffect implements IChromaticEffect {

    private final IChromaticEffect movement;
    private final GlyphGradient gradient;

    public GradientLayerEffect(IChromaticEffect movement, GlyphGradient gradient) {
        this.movement = movement;
        this.gradient = gradient;
    }

    public IChromaticEffect movement() {
        return movement;
    }

    public GlyphGradient gradient() {
        return gradient;
    }

    @Override
    public GlyphGradient getGlyphGradient() {
        return gradient;
    }

    // Colors come from the gradient; white keeps the style color from tinting anything.
    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        return 0xFFFFFF;
    }

    @Override
    public float getScale(float x, float y) {
        return movement.getScale(x, y);
    }

    @Override
    public float getScaleX(float x, float y) {
        return movement.getScaleX(x, y);
    }

    @Override
    public float getScaleY(float x, float y) {
        return movement.getScaleY(x, y);
    }

    @Override
    public float getXOffset(float x, float y) {
        return movement.getXOffset(x, y);
    }

    @Override
    public float getYOffset(float x, float y) {
        return movement.getYOffset(x, y);
    }

    @Override
    public boolean isOutline() {
        return movement.isOutline();
    }

    @Override
    public int getOutlineColor() {
        return movement.getOutlineColor();
    }

    @Override
    public boolean isVolumetric() {
        return movement.isVolumetric();
    }

    @Override
    public boolean isStatic() {
        return movement.isStatic();
    }

    @Override
    public boolean isAnimated() {
        return movement.isAnimated();
    }

    // useHorizontalBlending / isPassthrough stay false: the gradient renderer colors per vertex itself.
}
