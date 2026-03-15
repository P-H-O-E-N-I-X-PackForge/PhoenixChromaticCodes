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

        @Configurable
        @Configurable.Comment({
                "Add custom formatting codes here.",
                "Format: 'char:hex' (e.g., 'z:BF00FF')"
        })
        public String[] customColors = new String[] {
                "z:BF00FF",
        };

        @Configurable
        @Configurable.Comment({
                "Format: 'char:colorSpeed:moveSpeed:movementId:hex1,hex2...'",
                "Example: 'w:1.0:0.5:wave:rainbow'",
                "Movement IDs: wave, shake, pulse, static_rainbow, glitch, discord, breath, stretch, none",
                "Discord is the effect you see on servers with boosts, that smooth gradient. This one needs exactly two hex codes to work properly."
        })
        public String[] customGradients = new String[] {
                "w:1.0:1.0:wave:rainbow",        // Smooth rainbow wave
                "s:0.0:2.5:shake:FFFFFF",       // White violent shaking
                "p:1.0:2.0:pulse:FF0000,990000", // Red breathing pulse
                "*:1.5:0.0:static_rainbow:rainbow", // Entire word cycles rainbow
                "g:1.0:3.0:glitch:00FF00,005500", // Green matrix glitch
                "y:2.5:0.0:none:FF0000,FFFF00,00FF00", // Static horizontal gradient
                "^:1.0:0.0:discord:FC8EAC,8F00FF",
                "#:0.0:2.0:breath:FF00FF",
                "%:0.0:2.0:stretch:FF00FF"
        };
    }
}
