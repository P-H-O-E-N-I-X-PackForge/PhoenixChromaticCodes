package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.ColorHelper;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.List;

import static net.phoenix.chromatic_codes.api.ColorHelper.lerp;

public class ShiverEffect implements IChromaticEffect {
    private final float colorSpeed;
    private final float moveSpeed;
    private final List<Integer> colors;

    public ShiverEffect(float colorSpeed, float moveSpeed, List<Integer> colors) {
        this.colorSpeed = colorSpeed;
        this.moveSpeed = moveSpeed;
        this.colors = colors;
    }

    @Override
    public float getXOffset(float x, float y) {
        // moveSpeed scales the 'strength' of the jitter
        return (float) (Math.random() - 0.5) * 2.0f * moveSpeed;
    }

    @Override
    public float getYOffset(float x, float y) {
        return (float) (Math.random() - 0.5) * 2.0f * moveSpeed;
    }

    @Override
    public int getRenderColor(int original, float x, float y) {
        // Use colorSpeed for the gradient logic here
        return ColorHelper.getGradientColor(colors, colorSpeed, x);
    }
}