package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.IChromaticEffect;

public class CustomItalicEffect implements IChromaticEffect {

    private final float slantFactor;

    public CustomItalicEffect(float slantFactor) {
        this.slantFactor = slantFactor;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        return 0xFFFFFF;
    }

    public float getSlantFactor() {
        return this.slantFactor;
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
