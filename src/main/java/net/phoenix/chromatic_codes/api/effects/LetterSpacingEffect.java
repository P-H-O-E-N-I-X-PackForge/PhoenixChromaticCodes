package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.IChromaticEffect;

public class LetterSpacingEffect implements IChromaticEffect {

    private final float spacingCushion;

    public LetterSpacingEffect(float spacingCushion) {
        // spacingCushion represents extra pixels of width between letters (e.g., 2.0f or 4.0f)
        this.spacingCushion = spacingCushion;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        return 0xFFFFFF; // Defaults to white; overridden if combined with standard text styling
    }

    @Override
    public float getScale(float x, float y) {
        return 1.0f; // Kept normal size
    }

    // MixinFontSet reads getAdvance() * getScale(). We can creatively use scaleX
    // to pass down our specialized structural width extension!
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

    // Custom getter to clean up your MixinFontSet logic
    public float getSpacingCushion() {
        return this.spacingCushion;
    }
}
