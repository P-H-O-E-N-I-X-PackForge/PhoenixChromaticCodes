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
                "p:FF2100"
        };

        @Configurable
        @Configurable.Comment({
                "Format: 'char:speed:movementId:hex1,hex2...'",
                "Example: 'w:1.0:wave:rainbow'",
                "Movement IDs: wave, shake, none"
        })
        public String[] customGradients = new String[] {
                "w:1.0:wave:rainbow",        // Now configurable!
                "s:0.0:shake:FFFFFF",       // Shake is now white by default but configurable
                "y:2.5:none:FF0000,FFFF00,00FF00,00FFFF,0000FF,FF00FF"
        };
    }
}