package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.List;

public class PhoenixEffect implements IChromaticEffect {

    private final List<Integer> colors;

    public PhoenixEffect(float colorSpeed, float moveSpeed, List<Integer> colors) {
        this.colors = colors;
    }

    @Override
    public int getRenderColor(int original, float x, float y) {
        return colors.isEmpty() ? 0xFFFFFF : colors.get(0);
    }

    @Override
    public boolean isAnimated() {
        return true;
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
