package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.List;

public class VerticalGradientEffect implements IChromaticEffect {

    private final int topColor;
    private final int bottomColor;

    public VerticalGradientEffect(List<Integer> colors) {
        
        this.topColor = (colors != null && colors.size() > 0) ? colors.get(0) : 0xFFFFFF;
        this.bottomColor = (colors != null && colors.size() > 1) ? colors.get(1) : 0xAAAAAA;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        
        return this.topColor;
    }

    public int getTopColor() {
        return this.topColor;
    }

    public int getBottomColor() {
        return this.bottomColor;
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
