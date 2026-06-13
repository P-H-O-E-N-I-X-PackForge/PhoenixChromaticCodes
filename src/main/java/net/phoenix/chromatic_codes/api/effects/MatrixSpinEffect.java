package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.IChromaticEffect;

public class MatrixSpinEffect implements IChromaticEffect {

    private final float spinSpeed;

    public MatrixSpinEffect(float spinSpeed) {
        this.spinSpeed = spinSpeed > 0 ? spinSpeed : 1.0f;
    }

    // -------------------------------------------------------------------------
    // How the spin illusion works
    // -------------------------------------------------------------------------
    // A flat quad can't truly rotate in 3D, but we can fake a Y-axis spin with
    // two coordinated transforms applied every frame:
    //
    // 1. scaleX = |cos(θ)| — squishes the glyph to 0 at 90°, full at 0°/180°
    // 2. xOffset = shift(θ) — slides the glyph left or right so it appears to
    // pivot around its own center rather than its left edge
    //
    // Without the X offset the glyph just squishes in place (the old behavior).
    // With it, the left half "goes behind" and the right half "comes forward",
    // mimicking perspective foreshortening.
    //
    // We also flip the green channel at the halfway point (cos < 0) to give the
    // illusion that the back face is a darker tint — purely aesthetic.
    // -------------------------------------------------------------------------

    private double getAngle() {
        return (System.currentTimeMillis() * 0.003) * this.spinSpeed;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        double cos = Math.cos(getAngle());
        // Front face: white. Back face (cos < 0): desaturated green tint for a
        // "matrix screen" feel. Swap to 0x888888 for a plain grey back face.
        if (cos >= 0) {
            return 0x00FF41; // classic matrix green front
        } else {
            return 0x003B0F; // dark back face
        }
    }

    @Override
    public float getScaleX(float x, float y) {
        double cos = Math.cos(getAngle());
        float scale = (float) Math.abs(cos);
        // Clamp away from absolute zero to prevent GPU quad-discard artifacts
        return Math.max(scale, 0.001f);
    }

    // The pivot offset: when cos is positive the glyph is "front-facing" and needs
    // no lateral shift. As it rotates past 90° (cos going negative) the rendered
    // quad is the back face; we offset it so it still appears to spin around center.
    //
    // charHalfWidth isn't available here (it's a BakedGlyph field), so we use a
    // reasonable approximation of ~3px for the typical MC glyph half-width.
    // If you have glyphs of very different sizes consider passing width as a param.
    @Override
    public float getXOffset(float x, float y) {
        double cos = Math.cos(getAngle());
        float halfWidth = 3.0f; // approximate half-width of a standard MC glyph
        // When fully front-facing (cos=1): offset=0. At cos=0 (edge-on): offset=halfWidth.
        // This keeps the visual center stationary on screen.
        return (float) ((1.0 - Math.abs(cos)) * halfWidth * Math.signum(cos));
    }

    @Override
    public float getScale(float x, float y) {
        return 1.0f;
    }

    @Override
    public float getScaleY(float x, float y) {
        return 1.0f;
    }

    @Override
    public float getYOffset(float x, float y) {
        return 0;
    }
}
