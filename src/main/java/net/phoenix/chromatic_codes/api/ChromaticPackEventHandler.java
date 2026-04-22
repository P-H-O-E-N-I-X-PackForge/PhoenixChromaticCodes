package net.phoenix.chromatic_codes.api;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;

/**
 * Listens on the MOD event bus for AddPackFindersEvent and injects
 * ChromaticDynamicPack so Minecraft discovers our generated font JSONs
 * without any manual file creation.
 *
 * Register this class in your mod constructor or via @Mod.EventBusSubscriber:
 *
 * MinecraftForge.EVENT_BUS <-- wrong, this is the FORGE bus
 * FMLJavaModLoadingContext.get().getModEventBus() <-- correct, this is the MOD bus
 *
 * The easiest way is the @Mod.EventBusSubscriber annotation below.
 * Make sure ChromaticEffectsRegistry.init() is called BEFORE this fires
 * (i.e. in FMLClientSetupEvent or FMLCommonSetupEvent, which both fire earlier).
 */
@Mod.EventBusSubscriber(modid = PhoenixChromaticCodes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChromaticPackEventHandler {

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        // We only care about client resource packs (assets), not server datapacks
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        // Pack.readMetaAndCreate reads our pack.mcmeta via getRootResource,
        // then creates a Pack entry pointing at ChromaticDynamicPack.INSTANCE.
        Pack pack = Pack.readMetaAndCreate(
                ChromaticDynamicPack.INSTANCE.packId(),          // unique id string
                Component.literal("Chromatic Dynamic Fonts"),    // display name
                true,                                            // required = always active
                (id) -> ChromaticDynamicPack.INSTANCE,           // ResourcesSupplier
                PackType.CLIENT_RESOURCES,
                Pack.Position.TOP,                               // load on top so we aren't overridden
                PackSource.BUILT_IN                              // shows as built-in in the UI
        );

        if (pack != null) {
            event.addRepositorySource(consumer -> consumer.accept(pack));
        } else {
            PhoenixChromaticCodes.LOGGER.error(
                    "Phoenix Chromatic: Failed to create dynamic font pack — pack.mcmeta may be malformed.");
        }
    }
}
