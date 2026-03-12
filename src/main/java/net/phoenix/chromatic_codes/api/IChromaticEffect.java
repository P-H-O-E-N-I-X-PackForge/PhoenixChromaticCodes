package net.phoenix.chromatic_codes.api;

import net.minecraft.network.chat.Style;

public interface IChromaticEffect {
    // This is the method the Renderer calls
    default Style apply(Style style, float x, float y) {
        int color = getRenderColor(style.getColor() != null ? style.getColor().getValue() : 0xFFFFFF, x, y);
        // We apply the offsets internally or via the renderer
        return style.withColor(color);
    }

    int getRenderColor(int originalColor, float x, float y);

    default float getXOffset(float x, float y) { return 0; }
    default float getYOffset(float x, float y) { return 0; }
}