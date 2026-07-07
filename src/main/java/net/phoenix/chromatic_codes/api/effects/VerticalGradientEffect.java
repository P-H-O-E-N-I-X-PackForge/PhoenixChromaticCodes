package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.ColorHelper;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.List;

public class VerticalGradientEffect implements IChromaticEffect {

    // MixinBakedGlyph passes this.up/this.down (glyph-relative offsets from the
    // baseline) as y here rather than an absolute screen coordinate, so a fixed
    // glyph height assumption is used to normalize into a 0..1 range. These values
    // match Minecraft's default font metrics (ascent + descent = ~9px).
    private static final float GLYPH_ASCENT = 7.0f;
    private static final float GLYPH_HEIGHT = 9.0f;

    private final int topColor;
    private final int bottomColor;

    public VerticalGradientEffect(List<Integer> colors) {
        // Requires exactly 2 colors in the configuration array to map properly
        this.topColor = (colors != null && colors.size() > 0) ? colors.get(0) : 0xFFFFFF;
        this.bottomColor = (colors != null && colors.size() > 1) ? colors.get(1) : 0xAAAAAA;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        float factor = (y + GLYPH_ASCENT) / GLYPH_HEIGHT;
        factor = Math.max(0.0f, Math.min(1.0f, factor));
        return ColorHelper.lerp(this.topColor, this.bottomColor, factor);
    }

    // Direct explicit color interpolation helpers for top and bottom layout boundaries
    public int getTopColor() {
        return this.topColor;
    }

    public int getBottomColor() {
        return this.bottomColor;
    }

    @Override
    public float getScale(float x, float y) {
        return 1.0f;
    }

    @Override
    public float getScaleX(float x, float y) {
        return 1.0f;
    }

    @Override
    public float getScaleY(float x, float y) {
        return 1.0f;
    }

    @Override
    public float getXOffset(float x, float y) {
        return 0;
    }

    @Override
    public float getYOffset(float x, float y) {
        return 0;
    }
}
