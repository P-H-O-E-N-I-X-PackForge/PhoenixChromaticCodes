package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.awt.Color;

public class ChromaWaveEffect implements IChromaticEffect {

    private final float colorSpeed;

    public ChromaWaveEffect(float colorSpeed) {
        this.colorSpeed = colorSpeed > 0 ? colorSpeed : 1.0f;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        // x here is word-relative (0..~160 range from MixinBakedGlyph's blending path).
        // We need enough spatial spread that adjacent letters land on visibly different hues.
        // 0.015 gave a range of ~2.4 hue units across 160px — after %1.0 that's nearly
        // uniform. 0.008 per pixel gives ~1.28 full hue cycles across a 160px word, which
        // produces a clear visible wave.
        //
        // Time offset: 0.002 * speed was too slow to see motion. 0.0008 * speed gives a
        // comfortable scroll rate that's clearly animated but not seizure-inducing.
        double wave = (x * 0.008) - (System.currentTimeMillis() * 0.0008 * this.colorSpeed);

        // fmod into [0, 1)
        float hue = (float) (wave % 1.0);
        if (hue < 0) hue += 1.0f;

        return Color.HSBtoRGB(hue, 1.0f, 1.0f);
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

    @Override
    public boolean useHorizontalBlending() {
        // Must be true: left-edge and right-edge of each glyph get different hues so the
        // wave gradient flows smoothly through the letters instead of hard-stepping.
        return true;
    }
}
