package net.phoenix.chromatic_codes.network;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;
import net.phoenix.chromatic_codes.api.ScriptedCodes;

/** Script-defined codes belong to the server you're on; drop them when leaving it. */
@EventBusSubscriber(modid = PhoenixChromaticCodes.MOD_ID, value = Dist.CLIENT)
public final class ChromaticClientEvents {

    private ChromaticClientEvents() {}

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ScriptedCodes.clear();
    }
}
