package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.List;

public class Volumetric3DEffect implements IChromaticEffect {

    private final int frontColor;
    private final int sideColor;
    private final int depthLayers;

    public Volumetric3DEffect(float depth, List<Integer> colors) {
        this.frontColor = (colors != null && !colors.isEmpty()) ? colors.get(0) : 0xFFFFFF;
        this.sideColor = (colors != null && colors.size() > 1) ? colors.get(1) : 0x444444;
        this.depthLayers = depth > 0 ? (int) depth : 5;
    }

    private static final float LAYER_STEP = 0.6f;

    @Override
    public boolean isVolumetric() {
        return true;
    }

    public int getSideColor() {
        return this.sideColor;
    }

    public int getDepthLayers() {
        return this.depthLayers;
    }

    public float getLayerXShift() {
        return -LAYER_STEP;
    }

    public float getLayerYShift() {
        return -LAYER_STEP;
    }

    @Override
    public int getRenderColor(int c, float x, float y) {
        return this.frontColor;
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
