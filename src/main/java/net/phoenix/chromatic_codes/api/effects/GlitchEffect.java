package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.ColorHelper;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GlitchEffect implements IChromaticEffect {

    private final List<Integer> colors;
    private final float colorSpeed;
    private final float moveSpeed;

    public GlitchEffect(float colorSpeed, float moveSpeed, List<Integer> colors) {
        this.colors = colors;
        this.colorSpeed = colorSpeed;
        this.moveSpeed = moveSpeed;
    }

    @Override
    public int getRenderColor(int original, float x, float y) {
        // Flickers between colors rapidly based on speed
        if (moveSpeed > 0 && ThreadLocalRandom.current().nextFloat() < (0.1f * moveSpeed)) {
            return colors.get(ThreadLocalRandom.current().nextInt(colors.size()));
        }
        return ColorHelper.getGradientColor(colors, colorSpeed, x);
    }

    @Override
    public float getXOffset(float x, float y) {
        return (ThreadLocalRandom.current().nextFloat() < 0.05f * moveSpeed) ?
                (ThreadLocalRandom.current().nextFloat() - 0.5f) * 4f : 0;
    }

    @Override
    public float getYOffset(float x, float y) {
        return (ThreadLocalRandom.current().nextFloat() < 0.05f * moveSpeed) ?
                (ThreadLocalRandom.current().nextFloat() - 0.5f) * 4f : 0;
    }
}
