package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.IChromaticEffect;

public class CustomItalicEffect implements IChromaticEffect {

    private final float slantFactor;

    public CustomItalicEffect(float slantFactor) {
        // Vanilla defaults to 0.25f.
        // Setting moveSpeed config to 0.15f makes a gentle tilt; 0.45f makes an aggressive speed slant!
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
