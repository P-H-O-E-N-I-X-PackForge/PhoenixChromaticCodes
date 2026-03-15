package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.ColorHelper;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.List;

public class BreathEffect implements IChromaticEffect {

    private final List<Integer> colors;
    private final float pulseSpeed;

    public BreathEffect(float colorSpeed, float pulseSpeed, List<Integer> colors) {
        this.colors = colors;
        this.pulseSpeed = pulseSpeed;
    }

    @Override
    public int getRenderColor(int original, float x, float y) {
        int baseColor = colors.get(0);
        float wave = (float) (Math.sin(System.currentTimeMillis() * 0.005 * pulseSpeed) + 1.0) / 2.0f;
        return ColorHelper.lerp(0x333333, baseColor, 0.5f + (wave * 0.5f));
    }

    @Override
    public float getScale(float x, float y) {
        float wave = (float) Math.sin(System.currentTimeMillis() * 0.005 * pulseSpeed);
        return 1.0f + (wave * 0.15f);
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
