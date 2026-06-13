package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.awt.Color;

public class LiquidPlasmaEffect implements IChromaticEffect {

    private final float warpSpeed;

    public LiquidPlasmaEffect(float warpSpeed) {
        this.warpSpeed = warpSpeed > 0 ? warpSpeed : 1.0f;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        double time = (System.currentTimeMillis() * 0.002) * warpSpeed;
        float localX = x - ChromaticAPI.getSegmentStartX();

        // Overlapping sine-wave field equation to simulate dynamic fluid plasma
        double plasmaValue = Math.sin(localX * 0.05 + time) + Math.sin((y * 0.05 + time) * 1.5) +
                Math.sin((localX + y) * 0.02 + time);

        float hue = (float) ((plasmaValue / 6.0) + 0.5) % 1.0f;
        // Generate an incredibly rich, shifting cosmic color spectrum profile
        return Color.HSBtoRGB(hue, 0.9f, 1.0f);
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
