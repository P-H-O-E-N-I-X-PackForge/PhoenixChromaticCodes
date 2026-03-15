package net.phoenix.chromatic_codes.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.ChromaticAPI;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;
import net.phoenix.chromatic_codes.config.ModConfig;

import java.util.ArrayList;
import java.util.List;

import static net.phoenix.chromatic_codes.api.ChromaticColors.CUSTOM_FORMATTING;

public class ChromaticEffectsRegistry {

    public static void init() {

        ChromaticColors.init();


        parseAndRegister(ModConfig.INSTANCE.colors.customGradients);
    }

    public static void parseAndRegister(String[] configEntries) {
        if (configEntries == null || configEntries.length == 0) {
            PhoenixChromaticCodes.LOGGER.warn("Phoenix Chromatic: No gradient entries found in config!");
            return;
        }

        for (String entry : configEntries) {
            try {
                String[] parts = entry.split(":");
                if (parts.length < 5) continue;

                char code = parts[0].charAt(0);
                float colorSpeed = Float.parseFloat(parts[1]);
                float moveSpeed = Float.parseFloat(parts[2]);
                String movementId = parts[3].toLowerCase();
                List<Integer> colors = resolveColors(parts[4].toLowerCase());

                IChromaticEffect effect = MovementRegistry.create(movementId, colorSpeed, moveSpeed, colors);


                ResourceLocation fontId = PhoenixChromaticCodes.id(movementId);


                PhoenixChromaticCodes.LOGGER.info("Phoenix Chromatic: Registering §{} with effect {} on font {}", code,
                        movementId, fontId);

                ChromaticAPI.registerEffect(code, fontId, effect);
            } catch (Exception e) {
                PhoenixChromaticCodes.LOGGER.error("Failed to register chromatic effect: " + entry, e);
            }
        }
    }

    private static List<Integer> resolveColors(String colorData) {
        if (colorData.equals("rainbow")) {
            return List.of(0xFF0000, 0xFFFF00, 0x00FF00, 0x00FFFF, 0x0000FF, 0xFF00FF, 0xFF0000);
        }
        List<Integer> colors = new ArrayList<>();
        for (String hex : colorData.split(",")) {
            colors.add((int) Long.parseLong(hex.replace("#", "").trim(), 16));
        }
        return colors;
    }

    public static Component parseCustomEffects(String text) {
        MutableComponent root = Component.literal("");
        String[] parts = text.split("§");

        if (!parts[0].isEmpty()) root.append(Component.literal(parts[0]));

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;

            char code = Character.toLowerCase(part.charAt(0));
            String content = part.substring(1);
            MutableComponent segment = Component.literal(content);

            ResourceLocation dynamicFont = ChromaticAPI.getFontForCode(code);

            if (dynamicFont != null) {
                segment.withStyle(style -> style.withFont(dynamicFont).withColor((TextColor) null));
            } else if (CUSTOM_FORMATTING.containsKey(code)) {
                segment.withStyle(style -> style.withColor(TextColor.fromRgb(CUSTOM_FORMATTING.get(code))));
            } else {
                root.append(Component.literal("§" + part));
                continue;
            }
            root.append(segment);
        }
        return root;
    }
}
