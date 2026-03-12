package net.phoenix.chromatic_codes.api;

import net.minecraft.network.chat.Style;

public interface IChromaticEffect {
    int getRenderColor(int original, float x, float y);
    float getXOffset(float x, float y);
    float getYOffset(float x, float y);

    default Style apply(Style style, float x, float y) {
        // This is the bridge: it injects the color into the style
        return style.withColor(getRenderColor(0, x, y));
    }
}