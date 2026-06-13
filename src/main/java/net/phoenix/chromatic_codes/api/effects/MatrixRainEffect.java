package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.IChromaticEffect;

public class MatrixRainEffect implements IChromaticEffect {

    private final float dropSpeed;

    public MatrixRainEffect(float dropSpeed) {
        this.dropSpeed = dropSpeed > 0 ? dropSpeed : 1.0f;
    }

    @Override
    public int getRenderColor(int c, float x, float y) {
        return 0x00FF00;
    }

    // Intercepts UV shifting inside MixinBakedGlyph
    public float getVOffset() {
        // Glides the V texture loop parameters downward over time
        return (float) ((System.currentTimeMillis() * 0.001 * dropSpeed) % 1.0);
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
