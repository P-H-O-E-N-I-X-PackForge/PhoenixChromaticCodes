package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.List;

public class StretchEffect implements IChromaticEffect {

    private final List<Integer> colors;
    private final float pulseSpeed;

    public StretchEffect(float colorSpeed, float pulseSpeed, List<Integer> colors) {
        this.colors = colors;
        this.pulseSpeed = pulseSpeed;
    }

    @Override
    public int getRenderColor(int original, float x, float y) {
        return colors.get(0);
    }

    @Override
    public float getScale(float x, float y) {
        // Pulsates size between 0.8 and 1.3
        float wave = (float) Math.sin((System.currentTimeMillis() / 500.0) * pulseSpeed);
        return 1.0f + (wave * 0.25f);
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
