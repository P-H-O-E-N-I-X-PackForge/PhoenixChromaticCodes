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

    // -------------------------------------------------------------------------
    // Why the old version looked flat
    // -------------------------------------------------------------------------
    // The original code passed zOffset = -0.15f * i to buffer.vertex(..., z, ...).
    // In Minecraft's GUI rendering the projection is orthographic — the camera
    // never uses Z to make things look closer or farther. Depth only matters for
    // draw-order / z-fighting, not for visible perspective shift.
    //
    // A real isometric 3D illusion in a 2D GUI requires screen-space offsets:
    // • Each shadow layer shifts UP-LEFT by a fixed pixel amount per layer
    // (positive X and positive Y in screen space, since Y grows downward).
    // • The darkest layer is drawn first (furthest back), front face last.
    // • We keep z = 0 for all layers; draw order gives correct occlusion.
    //
    // The effect this produces: the front face looks like it's floating above a
    // solid block of "side" colored depth behind it, exactly like embossed text.
    // -------------------------------------------------------------------------

    /** Pixels of screen-space shift per depth layer (tune to taste). */
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

    // The screen-space isometric direction: up-left.
    // X shift: negative = left. Y shift: negative = up (screen Y grows down).
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
