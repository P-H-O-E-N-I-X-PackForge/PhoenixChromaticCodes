package net.phoenix.chromatic_codes.api;

public interface IChromaticEffect {
    int getRenderColor(int original, float x, float y);
    float getXOffset(float x, float y);
    float getYOffset(float x, float y);

    // Add this to fix the "Method does not override" error
    default boolean isStatic() {
        return false;
    }

    default float getScaleX(float x, float y) {
        return 1.0f;
    }

    default float getScaleY(float x, float y) {
        return 1.0f;
    }

    default float getScale(float x, float y) {
        return 1.0f;
    }

    default boolean isAnimated() {
        return false;
    }
}