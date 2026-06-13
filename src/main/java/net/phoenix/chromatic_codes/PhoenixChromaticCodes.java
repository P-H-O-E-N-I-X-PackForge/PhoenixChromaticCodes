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
        MovementRegistry.register("mirror", (cs, ms, colors) -> new MirroredGradientEffect(colors));      // Symmetric
                                                                                                          // Gloss
        MovementRegistry.register("vertical", (cs, ms, colors) -> new VerticalGradientEffect(colors));  // Top-to-Bottom
                                                                                                        // Split

        // --- New God-Tier Cosmetic Code Registrations ---
        MovementRegistry.register("rainbow_wave", (cs, ms, colors) -> new RainbowWaveEffect(cs, ms));
        MovementRegistry.register("outline", (cs, ms, colors) -> new TextOutlineEffect(colors));
        MovementRegistry.register("chroma", (cs, ms, colors) -> new ChromaGlitchEffect(colors));
        MovementRegistry.register("shear", (cs, ms, colors) -> new VerticalShearEffect(ms));

        // --- Wave 3: Advanced Spatial Morphs & Tickers ---
        MovementRegistry.register("chroma_wave", (cs, ms, colors) -> new ChromaWaveEffect(cs));
        MovementRegistry.register("wobble", (cs, ms, colors) -> new WobbleEffect(ms, colors));
        MovementRegistry.register("compress", (cs, ms, colors) -> new CompressEffect(ms));
        MovementRegistry.register("fade", (cs, ms, colors) -> new WordFadeEffect(colors));

        // --- Structural Layout & Scale Transform Enhancements ---
        MovementRegistry.register("size", (cs, ms, colors) -> new ScaleEffect((ms > 0.0f) ? ms : 1.5f));
        MovementRegistry.register("spacing", (cs, ms, colors) -> new LetterSpacingEffect((ms != 0.0f) ? ms : 2.0f));
        MovementRegistry.register("slant", (cs, ms, colors) -> new CustomItalicEffect((ms != 0.0f) ? ms : 0.25f));

        // Registry mappings
        MovementRegistry.register("3d", (cs, ms, colors) -> new Volumetric3DEffect(ms, colors));
        MovementRegistry.register("matrix_rain", (cs, ms, colors) -> new MatrixRainEffect(cs));
        MovementRegistry.register("3d_spin", (cs, ms, colors) -> new MatrixSpinEffect(ms));
        MovementRegistry.register("liquid_plasma", (cs, ms, colors) -> new LiquidPlasmaEffect(cs));

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
