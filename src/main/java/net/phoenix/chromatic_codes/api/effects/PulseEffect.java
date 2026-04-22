package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.ColorHelper;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.List;

public class PulseEffect implements IChromaticEffect {

    private final List<Integer> colors;
    private final float colorSpeed;
    private final float moveSpeed;

    public PulseEffect(float colorSpeed, float moveSpeed, List<Integer> colors) {
        this.colors = colors;
        this.colorSpeed = colorSpeed;
        this.moveSpeed = moveSpeed;
    }

    @Override
    public int getRenderColor(int original, float x, float y) {
        return ColorHelper.getGradientColor(colors, colorSpeed, x);
    }

    @Override
    public float getXOffset(float x, float y) {
        return 0;
    }

    @Override
    public float getYOffset(float x, float y) {
        // Creates a "breathing" effect where text moves up and down smoothly
        double time = (System.currentTimeMillis() % 1000000L) / 1000.0;
        return (float) Math.sin(time * moveSpeed * 3.0) * 2.0f;
    }
}
