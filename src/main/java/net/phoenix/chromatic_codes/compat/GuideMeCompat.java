package net.phoenix.chromatic_codes.compat;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.phoenix.chromatic_codes.config.ModConfig;

public final class GuideMeCompat {

    private GuideMeCompat() {}

    public static boolean shouldSkip() {
        ModConfig config = ModConfig.INSTANCE;
        if (config == null || !config.compatibility.disableInGuideME) return false;
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) return false;

        return ClientCheck.isGuideMeScreenOpen();
    }

    /**
     * Isolated so the dedicated server, which never passes the dist check above, never has to
     * classload (and therefore never touches) the client-only {@code Minecraft} class.
     */
    private static final class ClientCheck {

        static boolean isGuideMeScreenOpen() {
            net.minecraft.client.gui.screens.Screen screen = net.minecraft.client.Minecraft.getInstance().screen;
            if (screen == null) return false;

            return screen.getClass().getName().startsWith("guideme.");
        }
    }
}
