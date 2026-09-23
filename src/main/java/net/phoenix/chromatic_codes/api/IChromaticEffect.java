package net.phoenix.chromatic_codes.api;

public interface IChromaticEffect {

    int getRenderColor(int originalColor, float x, float y);

    default float getScale(float x, float y) {
        return 1.0f;
    }

    default float getScaleX(float x, float y) {
        return getScale(x, y);
    }

    default float getScaleY(float x, float y) {
        return getScale(x, y);
    }

    float getXOffset(float x, float y);

    float getYOffset(float x, float y);

    default boolean isOutline() {
        return false;
    }

    default int getOutlineColor() {
        return 0x000000;
    }

    default boolean isVolumetric() {
        return false;
    }

    default boolean isStatic() {
        return false;
    }

    default boolean isAnimated() {
        return false;
    }

    default boolean useHorizontalBlending() {
        return false;
    }

    default boolean isPassthrough() {
        return false;
    }
}
