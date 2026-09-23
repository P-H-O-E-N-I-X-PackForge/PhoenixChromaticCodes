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

        double wave = (x * 0.008) - (System.currentTimeMillis() * 0.0008 * this.colorSpeed);

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

        return true;
    }
}
