package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.List;

public class WordFadeEffect implements IChromaticEffect {

    private final List<Integer> colors;
    private final float maxSpan = 120.0f; // Bounding pixel width of the transparency sweep

    public WordFadeEffect(List<Integer> colors) {
        this.colors = colors;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        if (colors == null || colors.isEmpty()) return 0xFFFFFF;
        return colors.get(0);
    }

    // Custom helper getter that returns the alpha value (0.0 to 1.0) based on local position
    public float getDynamicAlpha(float x) {
        float localX = x - ChromaticAPI.getSegmentStartX();
        float progress = (localX < 0 ? 0 : localX) / maxSpan;

        // Clamped linear decay curve (Fully visible at start, fades down to 15% opacity at end)
        float alphaFactor = 1.0f - progress;
        return Math.max(0.15f, Math.min(1.0f, alphaFactor));
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
