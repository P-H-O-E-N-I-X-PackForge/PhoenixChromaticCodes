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

    public static class ColorConfig {

        // ---------------------------------------------------------------
        // VANILLA MINECRAFT FORMATTING CODES — DO NOT USE THESE AS KEYS
        // ---------------------------------------------------------------
        // Colors: 0-9, a-f
        // 0 black 1 dark_blue 2 dark_green 3 dark_aqua
        // 4 dark_red 5 dark_purple 6 gold 7 gray
        // 8 dark_gray 9 blue a green b aqua
        // c red d light_purple e yellow f white
        //
        // Formatting: k (obfuscated) l (bold) m (strikethrough)
        // n (underline) o (italic) r (reset)
        //
        // Safe single chars to use (not claimed by vanilla or common mods):
        // Letters: h i j q t u v z
        // (avoid x — vanilla uses it as the hex-color prefix)
        // Symbols: ! @ ^ * ( ) - _ = + | ; ' , . / ? ` ~
        // Note: FTB Quests reserve # and $ inside quest text.
        // They still work as codes in chat, books, and signs.
        // ---------------------------------------------------------------

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
                "Add custom single-character color codes here.",
                "Format: 'char:hex' (e.g., 'z:BF00FF')",
                "Do NOT use vanilla codes (0-9, a-f, k-o, r) as keys — see comment above."
        })
        public String[] customColors = new String[] {
                "z:BF00FF",
        };

        @Configurable
        @Configurable.Comment({
                "Add custom single-character gradient/effect codes here.",
                "Format: 'char:colorSpeed:moveSpeed:movementId:hex1,hex2...'",
                "Example: 'w:1.0:0.5:wave:rainbow'",
                "Movement IDs: wave, shake, pulse, static_rainbow, glitch, discord, breath, stretch, none",
                "Discord is the effect you see on servers with boosts, that smooth gradient. This one needs exactly two hex codes to work properly.",
                "Do NOT use vanilla codes (0-9, a-f, k-o, r) as keys — see comment above."
        })
        public String[] customGradients = new String[] {
                "w:1.0:1.0:wave:rainbow",           // Smooth rainbow wave
                "s:0.0:2.5:shake:FFFFFF",            // White violent shaking
                "p:1.0:2.0:pulse:FF0000,990000",     // Red breathing pulse
                "*:1.5:0.0:static_rainbow:rainbow",  // Entire word cycles rainbow
                "g:1.0:3.0:glitch:00FF00,005500",    // Green matrix glitch
                "y:2.5:0.0:none:FF0000,FFFF00,00FF00",
                "^:1.0:0.0:discord:FC8EAC,8F00FF",
                "u:0.0:2.0:breath:FF00FF",
                "%:0.0:2.0:stretch:FF00FF"
        };

        @Configurable
        @Configurable.Comment({
                "Named color codes using bracket syntax: '[name]:hex'",
                "Use as &[name] or §[name] in text.",
                "Example: '[phoenix]:BF00FF'  ->  use as &[phoenix] in chat",
                "Named codes are completely separate from single-char codes — no overlap ever."
        })
        public String[] namedColors = new String[] {
                "[phoenix]:BF00FF",
        };

        @Configurable
        @Configurable.Comment({
                "Named gradient/effect codes using bracket syntax.",
                "Format: '[name]:colorSpeed:moveSpeed:movementId:hex1,hex2...'",
                "Use as &[name] or §[name] in text.",
                "Movement IDs: wave, shake, pulse, static_rainbow, glitch, discord, breath, stretch, none",
                "Example: '[rainbow]:1.0:1.0:wave:rainbow'  ->  use as &[rainbow] in chat",
                "Named codes are completely separate from single-char codes — no overlap ever."
        })
        public String[] namedGradients = new String[] {
                "[rainbow]:1.0:1.0:wave:rainbow",
                "[matrix]:1.0:3.0:glitch:00FF00,005500",
        };
    }
}
