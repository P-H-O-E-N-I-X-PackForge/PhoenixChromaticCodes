package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.ColorHelper;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.List;

public class DiscordGradientEffect implements IChromaticEffect {

    private final List<Integer> colors;

    public DiscordGradientEffect(float colorSpeed, float moveSpeed, List<Integer> colors) {
        this.colors = colors;
    }

    @Override
    public int getRenderColor(int original, float x, float y) {
        // If it transitions too fast, divide x by a larger number here
        // to "slow down" the color change across the string.
        return ColorHelper.getDiscordGradient(colors, x);
    }

    @Override
    public boolean isStatic() {
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
