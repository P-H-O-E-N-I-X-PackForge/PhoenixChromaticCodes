package net.phoenix.chromatic_codes.api;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;

/**
 * Listens on the MOD event bus for AddPackFindersEvent and injects
 * ChromaticDynamicPack so Minecraft discovers our generated font JSONs
 * without any manual file creation.
 */
@EventBusSubscriber(modid = PhoenixChromaticCodes.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ChromaticPackEventHandler {

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        // We only care about client resource packs (assets), not server datapacks
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        // 1. Create the unified location info envelope containing ID, name, and source metadata
        net.minecraft.server.packs.PackLocationInfo locationInfo = new net.minecraft.server.packs.PackLocationInfo(
                ChromaticDynamicPack.INSTANCE.packId(),          // unique id string
                Component.literal("Chromatic Dynamic Fonts"),    // display name
                PackSource.BUILT_IN,                             // pack source metadata
                java.util.Optional.empty()                       // knownPackInfo (empty for dynamic packs)
        );

        // 2. Define the selection rules (always active on top)
        PackSelectionConfig selectionConfig = new PackSelectionConfig(
                true,                 // required = always active/forced
                Pack.Position.TOP,    // load on top so we aren't overridden
                false                 // fixed position (cannot be dragged down)
        );

        // 3. Modern 4-argument call signature with the exact 1.21.1 method mappings
        Pack pack = Pack.readMetaAndCreate(
                locationInfo,
                new Pack.ResourcesSupplier() {

                    @Override
                    public net.minecraft.server.packs.PackResources openPrimary(net.minecraft.server.packs.PackLocationInfo info) {
                        return ChromaticDynamicPack.INSTANCE;
                    }

                    @Override
                    public net.minecraft.server.packs.PackResources openFull(net.minecraft.server.packs.PackLocationInfo info,
                                                                             Pack.Metadata metadata) {
                        return ChromaticDynamicPack.INSTANCE;
                    }
                },
                PackType.CLIENT_RESOURCES,
                selectionConfig);

        if (pack != null) {
            event.addRepositorySource(consumer -> consumer.accept(pack));
        } else {
            PhoenixChromaticCodes.LOGGER.error(
                    "Phoenix Chromatic: Failed to create dynamic font pack — pack.mcmeta may be malformed.");
        }
    }
}
