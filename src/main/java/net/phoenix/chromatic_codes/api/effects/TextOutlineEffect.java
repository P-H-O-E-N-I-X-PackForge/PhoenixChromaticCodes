package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.List;

public class TextOutlineEffect implements IChromaticEffect {

    private final int textColor;
    private final int outlineColor;

    public TextOutlineEffect(List<Integer> colors) {
        this.textColor = (colors != null && !colors.isEmpty()) ? colors.get(0) : 0xFFFFFF;
        this.outlineColor = (colors != null && colors.size() > 1) ? colors.get(1) : 0x000000;
    }

    @Override
    public boolean isOutline() {
        return true;
    } // Notifies the Mixin Shadow Pass

    @Override
    public int getOutlineColor() {
        return this.outlineColor;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        return this.textColor;
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
