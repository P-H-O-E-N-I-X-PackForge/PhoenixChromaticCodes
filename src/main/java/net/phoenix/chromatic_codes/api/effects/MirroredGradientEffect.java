package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.ColorHelper;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.List;

public class MirroredGradientEffect implements IChromaticEffect {

    private final List<Integer> colors;
    private final float maxWidth = 100.0f; // The pixel span before the gradient maps cleanly out

    public MirroredGradientEffect(List<Integer> colors) {
        this.colors = colors;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        if (colors == null || colors.isEmpty()) return 0xFFFFFF;
        if (colors.size() == 1) return colors.get(0);

        // 1. Iso-lock position to the start of the word block
        float localX = x - ChromaticAPI.getSegmentStartX();

        // 2. Loop position progress periodically across the string bounding length
        float progress = (localX < 0 ? 0 : localX) / maxWidth;
        float factor = progress - (int) progress;

        // 3. Mirror the calculation at the 0.5 center point line
        if (factor > 0.5f) {
            factor = 1.0f - factor;
        }
        factor *= 2.0f; // Rescale back to a clean 0.0 -> 1.0 normalization mapping range

        // 4. Interpolate across color indices using zero colorSpeed to completely lock time ticking
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
