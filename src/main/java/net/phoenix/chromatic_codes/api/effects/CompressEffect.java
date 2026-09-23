package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.IChromaticEffect;

public class CompressEffect implements IChromaticEffect {

    private final float pulseSpeed;

    public CompressEffect(float pulseSpeed) {
        this.pulseSpeed = pulseSpeed > 0 ? pulseSpeed : 1.0f;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        return 0xFFFFFF;
    }

    @Override
    public float getScaleX(float x, float y) {
        double time = (System.currentTimeMillis() * 0.003) * pulseSpeed;
        
        return 1.0f + (float) Math.sin(time) * 0.3f;
    }

    @Override
    public float getScaleY(float x, float y) {
        double time = (System.currentTimeMillis() * 0.003) * pulseSpeed;
        
        return 1.0f - (float) Math.sin(time) * 0.2f;
    }

    @Override
    public float getScale(float x, float y) {
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
