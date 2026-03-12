package net.phoenix.chromatic_codes.api;

import net.minecraft.resources.ResourceLocation;

public class GradientEffect implements IChromaticEffect {
    private final int[] colors;
    private final float speed;
    private final ResourceLocation movementId;

    public GradientEffect(int[] colors, float speed, ResourceLocation movementId) {
        this.colors = colors;
        this.speed = speed;
        this.movementId = movementId;
    }

    @Override
    public int getRenderColor(int original, float x, float y) {
        if (colors.length == 0) return original;
        if (colors.length == 1) return colors[0];

        // Calculate progress based on time, speed, and X position (for the 'flow' effect)
        float time = (System.currentTimeMillis() / 1000f) * speed;
        float offset = x * 0.01f; // Adjust this to make the gradient "stretch" or "squish"
        float progress = (time + offset) % 1.0f;

        // Determine which two colors we are currently between
        float sectionProgress = progress * (colors.length - 1);
        int index = (int) sectionProgress;
        float factor = sectionProgress - index;

        return ColorHelper.lerp(colors[index], colors[index + 1], factor);
    }

    @Override
    public float getXOffset(float x, float y) {
        IChromaticEffect movement = ChromaticEffects.get(movementId);
        return movement != null ? movement.getXOffset(x, y) : 0;
    }

    @Override
    public float getYOffset(float x, float y) {
        IChromaticEffect movement = ChromaticEffects.get(movementId);
        return movement != null ? movement.getYOffset(x, y) : 0;
    }
}