package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.List;

public class WobbleEffect implements IChromaticEffect {

    private final List<Integer> colors;
    private final float wiggleSpeed;

    public WobbleEffect(float wiggleSpeed, List<Integer> colors) {
        this.colors = colors;
        this.wiggleSpeed = wiggleSpeed > 0 ? wiggleSpeed : 1.0f;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        if (colors == null || colors.isEmpty()) return 0xFFFFFF;
        return colors.get(0); // Uses primary assigned configuration color
    }

    @Override
    public float getXOffset(float x, float y) {
        double time = (System.currentTimeMillis() * 0.005) * wiggleSpeed;
        return (float) Math.sin(time + (x * 0.5)) * 1.2f;
    }

    @Override
    public float getYOffset(float x, float y) {
        double time = (System.currentTimeMillis() * 0.004) * wiggleSpeed;
        return (float) Math.cos(time + (x * 0.4)) * 1.4f;
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
}
