package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.IChromaticEffect;

public class MatrixSpinEffect implements IChromaticEffect {

    private final float spinSpeed;

    public MatrixSpinEffect(float spinSpeed) {
        this.spinSpeed = spinSpeed > 0 ? spinSpeed : 1.0f;
    }

    private double getAngle() {
        return (System.currentTimeMillis() * 0.003) * this.spinSpeed;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        double cos = Math.cos(getAngle());

        if (cos >= 0) {
            return 0x00FF41;
        } else {
            return 0x003B0F;
        }
    }

    @Override
    public float getScaleX(float x, float y) {
        double cos = Math.cos(getAngle());
        float scale = (float) Math.abs(cos);

        return Math.max(scale, 0.001f);
    }

    @Override
    public float getXOffset(float x, float y) {
        double cos = Math.cos(getAngle());
        float halfWidth = 3.0f;

        return (float) ((1.0 - Math.abs(cos)) * halfWidth * Math.signum(cos));
    }

    @Override
    public float getScale(float x, float y) {
        return 1.0f;
    }

    @Override
    public float getScaleY(float x, float y) {
        return 1.0f;
    }

    @Override
    public float getYOffset(float x, float y) {
        return 0;
    }
}
