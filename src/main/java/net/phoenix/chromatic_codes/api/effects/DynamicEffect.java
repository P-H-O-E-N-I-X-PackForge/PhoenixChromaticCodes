package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.ColorHelper;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.List;

public class DynamicEffect implements IChromaticEffect {

    private final List<Integer> colors;
    private final float colorSpeed;
    private final float moveSpeed;
    private final String movementType;

    public DynamicEffect(List<Integer> colors, float colorSpeed, float moveSpeed, String movementType) {
        this.colors = colors;
        this.colorSpeed = colorSpeed;
        this.moveSpeed = moveSpeed;
        this.movementType = movementType;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        return ColorHelper.getGradientColor(colors, colorSpeed, x);
    }

    @Override
    public float getXOffset(float x, float y) {
        return 0;
    }

    @Override
    public float getYOffset(float x, float y) {
        if (movementType.equals("wave")) {
            long time = System.currentTimeMillis() % 1000000L;
            return (float) Math.sin((time / 150.0) * moveSpeed + (x * 0.1)) * 1.5f;
        }
        return 0;
    }
}
