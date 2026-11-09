package net.phoenix.chromatic_codes.api;

import net.minecraft.resources.ResourceLocation;
import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.effects.GradientFontEffect;

import java.util.function.Supplier;

/**
 * Registers a font as a Chromatic code, from any mod and any namespace. Obtain one with
 * {@link ChromaticAPI#registerFont(String, ResourceLocation)} and finish with {@link #register()}:
 * <pre>{@code
 * ChromaticAPI.registerFont("fancy", new ResourceLocation("myaddon", "fancy"))  // font JSON: assets/myaddon/font/fancy.json
 *         .yOffset(1.0f)                  // optional vertical correction, GUI px (+ = down)
 *         .register();
 * }</pre>
 * That gives {@code &[fancy]}, lets other effects layer over it ({@code &[fancy]&[wave]}), allows {@code @fancy} in
 * config lines / inline codes / KubeJS {@code font:}, and makes vanilla color codes keep the font.
 * <p>
 * Call it from your mod constructor (or earlier). The name is lowercased and may use letters, digits, {@code _ . -}.
 */
public final class FontRegistration {

    private final String name;
    private final ResourceLocation fontId;
    private IChromaticEffect effect;
    private Supplier<Float> yOffset;
    private ResourceLocation plainVariant;

    public FontRegistration(String name, ResourceLocation fontId) {
        this.name = name;
        this.fontId = fontId;
    }

    /** An effect the font brings with it (e.g. a baked-in gradient). Movement effects layered over it keep their motion. */
    public FontRegistration effect(IChromaticEffect effect) {
        this.effect = effect;
        return this;
    }

    /** Shortcut for a font whose color comes from a gradient in its own glyph space (see {@link GlyphGradient}). */
    public FontRegistration gradient(GlyphGradient gradient) {
        return effect(new GradientFontEffect(gradient));
    }

    /** Vertical correction for glyphs of this font, in GUI px; positive moves them down. */
    public FontRegistration yOffset(float px) {
        return yOffset(() -> px);
    }

    /** Same, read live (for a config value). */
    public FontRegistration yOffset(Supplier<Float> px) {
        this.yOffset = px;
        return this;
    }

    /**
     * The font a vanilla color code (&c etc.) switches to. Needed only for fonts with a baked-in effect that can't
     * coexist with a text color; without it the font simply stays as is.
     */
    public FontRegistration plainVariant(ResourceLocation plainFont) {
        this.plainVariant = plainFont;
        return this;
    }

    public void register() {
        ChromaticAPI.registerResourceFontNamed(name, fontId, effect);
        if (yOffset != null) ChromaticAPI.setBaseFontYOffset(fontId, yOffset);
        if (plainVariant != null) ChromaticAPI.setPlainVariant(fontId, plainVariant);
    }
}
