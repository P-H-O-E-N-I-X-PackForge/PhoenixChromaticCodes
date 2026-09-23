package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.ColorHelper;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.List;

public class MirroredGradientEffect implements IChromaticEffect {

    private final List<Integer> colors;
    private final float maxWidth = 100.0f;

    public MirroredGradientEffect(List<Integer> colors) {
        this.colors = colors;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        if (colors == null || colors.isEmpty()) return 0xFFFFFF;
        if (colors.size() == 1) return colors.get(0);

        float localX = x - ChromaticAPI.getSegmentStartX();

        float progress = (localX < 0 ? 0 : localX) / maxWidth;
        float factor = progress - (int) progress;

        if (factor > 0.5f) {
            factor = 1.0f - factor;
        }
        factor *= 2.0f;

        return ColorHelper.getGradientColor(colors, 0.0f, factor * 100.0f);
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
