package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.IChromaticEffect;

public class LetterSpacingEffect implements IChromaticEffect {

    private final float spacingCushion;

    public LetterSpacingEffect(float spacingCushion) {
        this.spacingCushion = spacingCushion;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        return 0xFFFFFF;   // unused: isPassthrough() leaves color to the style (or a layered effect) instead
    }

    @Override
    public boolean isPassthrough() {
        return true;
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

    public float getSpacingCushion() {
        return this.spacingCushion;
    }
}
