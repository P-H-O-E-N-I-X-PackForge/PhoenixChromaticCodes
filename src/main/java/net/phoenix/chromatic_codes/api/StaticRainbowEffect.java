package net.phoenix.chromatic_codes.api;

import java.util.List;

public class StaticRainbowEffect implements IChromaticEffect {
    private final List<Integer> colors;
    private final float colorSpeed;
    private final float moveSpeed;

    public StaticRainbowEffect(float colorSpeed, float moveSpeed, List<Integer> colors) {
        this.colors = colors;
        this.colorSpeed = colorSpeed;
        this.moveSpeed = moveSpeed;
    }

    @Override
    public int getRenderColor(int original, float x, float y) {
        // By passing 0 instead of 'x', the whole string cycles colors in unison
        return ColorHelper.getGradientColor(colors, colorSpeed, 0);
    }

    @Override
    public float getXOffset(float x, float y) { return 0; }

    @Override
    public float getYOffset(float x, float y) { return 0; }
}