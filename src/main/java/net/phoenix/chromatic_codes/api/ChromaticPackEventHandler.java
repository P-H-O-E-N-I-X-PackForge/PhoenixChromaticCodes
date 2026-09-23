package net.phoenix.chromatic_codes.api;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;

@Mod.EventBusSubscriber(modid = PhoenixChromaticCodes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChromaticPackEventHandler {

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        Pack pack = Pack.readMetaAndCreate(
                ChromaticDynamicPack.INSTANCE.packId(),          
                Component.literal("Chromatic Dynamic Fonts"),    
                true,                                            
                (id) -> ChromaticDynamicPack.INSTANCE,           
                PackType.CLIENT_RESOURCES,
                Pack.Position.TOP,                               
                PackSource.BUILT_IN                              
        );

        if (pack != null) {
            event.addRepositorySource(consumer -> consumer.accept(pack));
        } else {
            PhoenixChromaticCodes.LOGGER.error(
                    "Phoenix Chromatic: Failed to create dynamic font pack — pack.mcmeta may be malformed.");
        }
    }
}
