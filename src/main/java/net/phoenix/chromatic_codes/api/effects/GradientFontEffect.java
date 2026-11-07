package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.GlyphGradient;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

/** Paints a font-space {@link GlyphGradient} on a font's glyphs; no movement, no scaling. */
public class GradientFontEffect implements IChromaticEffect {

    private final GlyphGradient gradient;

    public GradientFontEffect(GlyphGradient gradient) {
        this.gradient = gradient;
    }

    @Override
    public GlyphGradient getGlyphGradient() {
        return gradient;
    }

    // The renderer colors from the gradient; white here just keeps any non-gradient code path from tinting.
    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        return 0xFFFFFF;
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
