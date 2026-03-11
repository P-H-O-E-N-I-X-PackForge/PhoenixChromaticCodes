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
                "Format: 'char:hex' (e.g., 'z:BF00FF')",
                "Note: The code is a single character, and hex should not include #"
        })
        public String[] customColors = new String[] {
                "z:BF00FF",
                "°:00F2FF",
                "p:FF2100"
        };
    }
}
