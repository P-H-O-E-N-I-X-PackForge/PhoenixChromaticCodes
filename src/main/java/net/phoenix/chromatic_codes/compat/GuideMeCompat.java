package net.phoenix.chromatic_codes.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.phoenix.chromatic_codes.config.ModConfig;

/**
 * Detects whether a GuideME (modId "guideme") guidebook screen is currently
 * open, so chromatic code parsing can be skipped for its plain descriptive
 * text. GuideME is an optional dependency — this class only inspects the
 * active screen's class name, so it works whether or not GuideME is loaded.
 */
public final class GuideMeCompat {

    private GuideMeCompat() {}

    public static boolean shouldSkip() {
        if (!ModConfig.INSTANCE.compatibility.disableInGuideME) return false;

        Screen screen = Minecraft.getInstance().screen;
        if (screen == null) return false;

        return screen.getClass().getName().startsWith("guideme.");
    }
}
