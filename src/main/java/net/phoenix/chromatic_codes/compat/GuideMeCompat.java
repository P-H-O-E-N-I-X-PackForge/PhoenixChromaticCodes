package net.phoenix.chromatic_codes.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.phoenix.chromatic_codes.config.ModConfig;

public final class GuideMeCompat {

    private GuideMeCompat() {}

    public static boolean shouldSkip() {
        ModConfig config = ModConfig.INSTANCE;
        if (config == null || !config.compatibility.disableInGuideME) return false;

        Screen screen = Minecraft.getInstance().screen;
        if (screen == null) return false;

        return screen.getClass().getName().startsWith("guideme.");
    }
}
