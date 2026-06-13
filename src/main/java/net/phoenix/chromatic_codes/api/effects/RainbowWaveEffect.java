package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.awt.Color;

public class RainbowWaveEffect implements IChromaticEffect {

    private final float waveSpeed;
    private final float colorSpeed;

    public RainbowWaveEffect(float colorSpeed, float waveSpeed) {
        this.colorSpeed = colorSpeed > 0 ? colorSpeed : 1.0f;
        this.waveSpeed = waveSpeed > 0 ? waveSpeed : 1.0f;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        long time = System.currentTimeMillis();
        // Calculate spatial color shift across the word
        float localX = x - ChromaticAPI.getSegmentStartX();
        float hue = ((time * 0.002f * colorSpeed) + (localX * 0.01f)) % 1.0f;
        return Color.HSBtoRGB(hue, 0.85f, 1.0f);
    }

    @Override
    public float getYOffset(float x, float y) {
        long time = System.currentTimeMillis() % 1000000L;
        // Independent mathematical offset wave running parallel to color changes
        return (float) Math.sin((time / 150.0) * waveSpeed + (x * 0.1f)) * 2.0f;
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
}
