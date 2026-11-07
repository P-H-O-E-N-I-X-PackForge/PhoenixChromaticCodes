package net.phoenix.chromatic_codes.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;

/** Pushes script-defined codes to players when they join and after every {@code /reload}. */
@EventBusSubscriber(modid = PhoenixChromaticCodes.MOD_ID)
public final class ChromaticNetworkEvents {

    private ChromaticNetworkEvents() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) ChromaticNetwork.sendTo(player);
    }

    // Fired after a reload finished (all players) and for a single joining player; re-sending is idempotent.
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.getRelevantPlayers().forEach(ChromaticNetwork::sendTo);
    }
}
