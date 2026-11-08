package net.phoenix.chromatic_codes.network;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;
import net.phoenix.chromatic_codes.api.ScriptedCodes;

/** Script-defined codes belong to the server you're on; drop them when leaving it. */
@Mod.EventBusSubscriber(modid = PhoenixChromaticCodes.MOD_ID, value = Dist.CLIENT)
public final class ChromaticClientEvents {

    private ChromaticClientEvents() {}

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ScriptedCodes.clear();
    }
}
