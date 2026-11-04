package net.phoenix.chromatic_codes.config;

import net.phoenix.chromatic_codes.PhoenixChromaticCodes;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.ConfigHolder;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(id = PhoenixChromaticCodes.MOD_ID)
public class ModConfig {

    public static ModConfig INSTANCE;
    public static ConfigHolder<ModConfig> CONFIG_HOLDER;

    public static void init() {
        CONFIG_HOLDER = Configuration.registerConfig(ModConfig.class, ConfigFormats.yaml());
        INSTANCE = CONFIG_HOLDER.getConfigInstance();
    }

    @Configurable
    public ColorConfig colors = new ColorConfig();

    @Configurable
    public CompatibilityConfig compatibility = new CompatibilityConfig();

    public static class CompatibilityConfig {

        @Configurable
        @Configurable.Comment({
                "Disable Chromatic Codes parsing while a GuideME guidebook screen is open.",
                "GuideME pages are plain descriptive text, so an '&' immediately followed by",
                "a registered code letter (e.g. an ingredient list reading '... & Steel' when",
                "'s' is a registered code) would otherwise be misread as a chromatic code and",
                "break the guide's formatting. Chromatic codes keep working everywhere else",
                "(chat, tooltips, signs, books, other GUIs).",
                "Default: true"
        })
        public boolean disableInGuideME = true;
    }

    public static class ColorConfig {

        @Configurable
        @Configurable.Comment({
                "Whether named bracket codes are case-sensitive.",
                "true  -> &[Phoenix] and &[phoenix] are two completely different codes.",
                "false -> &[Phoenix] and &[phoenix] both resolve to whichever was registered first (lowercased).",
                "Requires a game restart to take effect.",
                "Default: true"
        })
        public boolean namedCodesCaseSensitive = true;

        @Configurable
        @Configurable.Comment({
                "Allow one-off inline definitions written straight into text (chat, signs, books...),",
                "so players can make a gradient/effect on the spot without it being defined in this config.",
                "Syntax (works with & or §):",
                "  &[~FF0000,0000FF]                 static gradient (3-digit hex works too: &[~f00,00f])",
                "  &[~wave:FF0000,00FF00]            movement:colors",
                "  &[~1.5:2.0:wave:FF0000,00FF00]    colorSpeed:moveSpeed:movement:colors (same order as the lists above)",
                "  &[~rainbow]  /  &[~wave:rainbow]  'rainbow' can be used as the color list",
                "  &[~size]  /  &[~spacing]          a movement id on its own uses that movement's defaults",
                "Movement IDs are the same as in the gradient lists ('outline' is not supported inline).",
                "Limits: at most 16 colors, speeds clamped to +-32, '3d' depth capped at 16.",
                "Set to false if only codes defined in this config should be usable (e.g. in a modpack).",
                "Default: true"
        })
        public boolean allowInlineCodes = true;

        @Configurable
        @Configurable.Comment({
                "Add custom single-character color codes here.",
                "Format: 'char:hex' (e.g., 'z:BF00FF')",
                "Do NOT use vanilla codes (0-9, a-f, k-o, r) as keys."
        })
        // The in-game config GUI's per-entry text box defaults to 32 characters; without this,
        // editing an entry there silently truncates it past that length.
        @Configurable.Gui.CharacterLimit(256)
        public String[] customColors = new String[] {
                "z:BF00FF",
                "t:00FF80",
                "s:0B585C"
        };

        @Configurable
        @Configurable.Comment({
                "Add custom single-character gradient/effect codes here.",
                "Format: 'char:colorSpeed:moveSpeed:movementId:hex1,hex2...'",
                "Movement IDs: wave, shake, pulse, static_rainbow, glitch, discord, breath, stretch, none, mirror, vertical, spacing, slant, rainbow_wave, chroma, shear, chroma_wave, wobble, compress, fade, 3d, matrix_rain, 3d_spin, liquid_plasma",
                "Do NOT use vanilla codes (0-9, a-f, k-o, r) as keys."
        })
        @Configurable.Gui.CharacterLimit(256)
        public String[] customGradients = new String[] {
                "w:1.0:1.0:wave:rainbow",
                "p:1.0:2.0:pulse:FF0000,990000",
                "*:1.5:0.0:static_rainbow:rainbow",
                "g:1.0:3.0:glitch:00FF00,005500",
                "y:2.5:0.0:none:FF0000,FFFF00,00FF00",
                "^:0.0:0.0:discord:BF00FF,FFB7C5",
                "$:0.0:2.0:breath:FF00FF",
                "-:0.0:2.0:stretch:FF00FF"
        };

        @Configurable
        @Configurable.Comment({
                "Named color codes using bracket syntax: '[name]:hex'",
                "Use as &[name] or §[name] in text.",
                "Example: '[phoenix]:BF00FF'  ->  use as &[phoenix] in chat",
                "Named codes are completely separate from single-char codes — no overlap ever."
        })
        @Configurable.Gui.CharacterLimit(256)
        public String[] namedColors = new String[] {
                "[phoenix]:BF00FF",
        };

        @Configurable
        @Configurable.Comment({
                "Named gradient/effect codes using bracket syntax.",
                "Format: '[name]:colorSpeed:moveSpeed:movementId:hex1,hex2...'",
                "Use as &[name] or §[name] in text.",
                "Movement IDs: wave, shake, pulse, static_rainbow, glitch, discord, breath, stretch, none, mirror, vertical, spacing, slant, rainbow_wave, chroma, shear, chroma_wave, wobble, compress, fade, 3d, matrix_rain, 3d_spin, liquid_plasma",
                "SPECIAL MODIFIERS GUIDE:",
                " - 'size': 'moveSpeed' acts as a text scaling multiplier. (e.g., [big]:0.0:1.5:size:FFFFFF)",
                " - 'spacing': 'moveSpeed' adds horizontal pixel padding gaps between characters. (e.g., [spaced]:0.0:4.0:spacing:FFFFFF)",
                " - 'slant': 'moveSpeed' defines custom italic angle parameters. Vanilla is 0.25f. (e.g., [speed]:0.0:0.45:slant:FFFFFF)",
                " - 'shear': 'moveSpeed' controls vertical progressive staircase step ratios. (e.g., [uphill]:0.0:0.2:shear:FFFFFF)",
                " - '3d': 'moveSpeed' specifies the number of stacked structural extrusion layers. (e.g., [3d_block]:0.0:5.0:3d:FFCC00,443300)"
        })
        @Configurable.Gui.CharacterLimit(256)
        public String[] namedGradients = new String[] {
                "[rainbow]:1.0:1.0:wave:rainbow",
                "[matrix]:1.0:3.0:glitch:00FF00,005500",
                "[big]:0.0:1.5:size:FFFFFF",
                "[huge]:0.0:2.5:size:FFFFFF",
                "[frozen]:0.0:0.0:none:FF00FF,00FFFF",
                "[glossy]:0.0:0.0:mirror:FF0000,00FF00,0000FF",
                "[sunset]:0.0:0.0:vertical:FFaa00,FF00aa",
                "[spaced]:0.0:4.0:spacing:FFFFFF",
                "[speed]:0.0:0.45:slant:FFFFFF",
                "[neon_ocean]:1.2:1.0:rainbow_wave:rainbow",
                "[cyber]:1.0:1.0:chroma:FF0055,00FFFF",
                "[uphill]:0.0:0.2:shear:FFFFFF",
                "[rgb_ticker]:1.5:0.0:chroma_wave:rainbow",
                "[jelly]:0.0:1.2:wobble:00FFaa",
                "[elastic]:0.0:1.0:compress:FFFFFF",
                "[ghost]:0.0:0.0:fade:FFFFFF",
                "[3d_block]:0.0:5.0:3d:FFCC00,443300",
                "[digital]:1.5:0.0:matrix_rain:00FF00",
                "[flip]:0.0:1.5:3d_spin:FFFFFF",
                "[plasma]:1.5:0.0:liquid_plasma:FFFFFF"
        };
    }
}
