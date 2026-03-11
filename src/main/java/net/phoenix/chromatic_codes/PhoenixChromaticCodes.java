package net.phoenix.chromatic_codes;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.phoenix.chromatic_codes.api.ChromaticColors;
import net.phoenix.chromatic_codes.config.ModConfig;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.format.ConfigFormats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PhoenixChromaticCodes.MOD_ID)
public class PhoenixChromaticCodes {

    public static final String MOD_ID = "phoenix_chromatic_codes";
    public static final Logger LOGGER = LogManager.getLogger();
    // public static final Registrate CHROMATIC_REGISTRATE = Registrate.create(MOD_ID);

    public PhoenixChromaticCodes(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModConfig.init();
        // modEventBus.register(CHROMATIC_REGISTRATE);
        // TestingBlocks.init();
        ChromaticColors.init();

        // Configuration
        Configuration.registerConfig(ModConfig.class, ConfigFormats.yaml());

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Phoenix's Chromatic Codes is heating up!");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
