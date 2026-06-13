package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.IChromaticEffect;

public class ScaleEffect implements IChromaticEffect {

    private final float scale;

    public ScaleEffect(float scale) {
        this.scale = scale;
    }

    @Override
    public int getRenderColor(int original, float x, float y) {
        // Return white (0xFFFFFF) so vanilla passes a valid color to BakedGlyph
        return 0xFFFFFF;
    }

    @Override
    public float getScale(float x, float y) {
        return this.scale; // e.g., 1.5f for 50% bigger
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
        // OPTIONAL: Adjust Y slightly downward when scaling up
        // to keep the text aligned nicely with the text baseline.
        return 0;
    }
}
