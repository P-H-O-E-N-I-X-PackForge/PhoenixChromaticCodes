package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

public class VerticalShearEffect implements IChromaticEffect {

    private final float shearSlope;

    public VerticalShearEffect(float shearSlope) {
        
        this.shearSlope = shearSlope != 0.0f ? shearSlope : -0.2f;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        return 0xFFFFFF;
    }

    @Override
    public float getYOffset(float x, float y) {
        
        float localX = x - ChromaticAPI.getSegmentStartX();
        return localX * this.shearSlope;
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
}
