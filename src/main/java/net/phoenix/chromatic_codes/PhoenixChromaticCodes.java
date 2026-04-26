package net.phoenix.chromatic_codes;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.phoenix.chromatic_codes.api.*;
import net.phoenix.chromatic_codes.api.effects.*;
import net.phoenix.chromatic_codes.config.ModConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PhoenixChromaticCodes.MOD_ID)
public class PhoenixChromaticCodes {

    public static final String MOD_ID = "phoenix_chromatic_codes";
    public static final Logger LOGGER = LogManager.getLogger();

    public PhoenixChromaticCodes(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModConfig.init();

        MovementRegistry.register("wave", (cs, ms, colors) -> new DynamicEffect(colors, cs, ms, "wave"));
        MovementRegistry.register("shake", ShiverEffect::new);
        MovementRegistry.register("none", (cs, ms, colors) -> new DynamicEffect(colors, cs, ms, "none"));
        MovementRegistry.register("pulse", PulseEffect::new);
        MovementRegistry.register("static_rainbow", StaticRainbowEffect::new);
        MovementRegistry.register("glitch", GlitchEffect::new);
        MovementRegistry.register("breath", BreathEffect::new);
        MovementRegistry.register("stretch", StretchEffect::new);

        // 2. SECOND: Now init the registry so it can find the types above
        ChromaticEffectsRegistry.init();

        ChromaticColors.init();

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Phoenix's Chromatic Codes is heating up!");
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
