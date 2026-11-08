package net.phoenix.chromatic_codes.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;

/** Pushes script-defined codes to players when they join and after every {@code /reload}. */
@Mod.EventBusSubscriber(modid = PhoenixChromaticCodes.MOD_ID)
public final class ChromaticNetworkEvents {

    private ChromaticNetworkEvents() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ChromaticNetwork.sendTo(player);
        }
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            // Sent for a single player joining
            ChromaticNetwork.sendTo(event.getPlayer());
        } else {
            // Sent when a global /reload command is run; sync to all connected players
            for (ServerPlayer player : event.getPlayerList().getPlayers()) {
                ChromaticNetwork.sendTo(player);
            }
        }
    }
}
