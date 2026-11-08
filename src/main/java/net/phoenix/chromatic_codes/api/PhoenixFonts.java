package net.phoenix.chromatic_codes.api;

import net.minecraft.resources.ResourceLocation;
import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;
import net.phoenix.chromatic_codes.api.effects.GradientFontEffect;
import net.phoenix.chromatic_codes.config.ModConfig;

/**
 * Codes for the bundled Phoenix Display fonts.
 * 
 * <pre>
 *   &amp;[display]        Phoenix Display, text colored like any other text (&amp;c, &amp;[phoenix], ...)
 *   &amp;[display_color]  Phoenix Display with its built-in purple-to-magenta gradient
 * </pre>
 * 
 * Both load the same TrueType file at the same size, so spacing is identical; the color one only differs by the
 * gradient painted by the glyph renderer. Registered before the config is read, so a config entry with the same name
 * overrides these. As with every code, a vanilla color code (&amp;c etc.) switches back to the default font.
 */
public final class PhoenixFonts {

    public static final ResourceLocation DISPLAY = PhoenixChromaticCodes.id("phoenix_display");
    public static final ResourceLocation DISPLAY_COLOR = PhoenixChromaticCodes.id("phoenix_display_color");

    public static final String DISPLAY_NAME = "display";
    public static final String DISPLAY_COLOR_NAME = "display_color";

    // Must match "size" in font/phoenix_display*.json.
    private static final float EM_PX = 10.5f;

    // Straight from PhoenixDisplay-Color.ttf's COLR table (font units, 1000/em): one linear gradient, pad extend.
    private static final GlyphGradient COLOR_GRADIENT = new GlyphGradient(
            100f, 45f, 720f, 750f, EM_PX, 1000f,
            new float[] { 0.0f, 0.489990234375f, 0.78997802734375f, 1.0f },
            new int[] { 0x2B006F, 0x7200C2, 0xA900D8, 0xDF0FE8 },
            () -> ModConfig.INSTANCE == null ? 0f : ModConfig.INSTANCE.fonts.displayFontYOffset);

    private PhoenixFonts() {}

    public static void init() {
        ChromaticAPI.registerResourceFontNamed(DISPLAY_NAME, DISPLAY, null);
        ChromaticAPI.registerResourceFontNamed(DISPLAY_COLOR_NAME, DISPLAY_COLOR,
                new GradientFontEffect(COLOR_GRADIENT));

        // One tunable vertical correction for both (config: fonts.displayFontYOffset, GUI px, + = down)
        java.util.function.Supplier<Float> yOffset = () -> ModConfig.INSTANCE == null ? 0f :
                ModConfig.INSTANCE.fonts.displayFontYOffset;
        // &c etc. over the color font falls back to its plain sibling so the color actually applies
        ChromaticAPI.setPlainVariant(DISPLAY_COLOR, DISPLAY);
        ChromaticAPI.setBaseFontYOffset(DISPLAY, yOffset);
        ChromaticAPI.setBaseFontYOffset(DISPLAY_COLOR, yOffset);
    }
}
