package net.phoenix.chromatic_codes.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;
import net.phoenix.chromatic_codes.config.ModConfig;

import java.util.ArrayList;
import java.util.List;

import static net.phoenix.chromatic_codes.api.ChromaticColors.CUSTOM_FORMATTING;

public class ChromaticEffectsRegistry {

    public static void init() {
        ChromaticColors.init();
        parseAndRegister(ModConfig.INSTANCE.colors.customGradients);
        parseAndRegisterNamed(ModConfig.INSTANCE.colors.namedGradients);
    }

    // -------------------------------------------------------------------------
    // Formatting registration
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Color/effect registration (unchanged from original)
    // -------------------------------------------------------------------------

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

                // 'outline' was previously a movementId here; it is now a formatting
                // modifier in the [formatting] section. Skip silently with a warning
                // so old configs don't crash.
                if (movementId.equals("outline")) {
                    PhoenixChromaticCodes.LOGGER.warn(
                            "Phoenix Chromatic: Entry '{}' uses movementId 'outline', which has moved " +
                                    "to the [formatting] section of the config. This entry has been ignored.",
                            entry);
                    continue;
                }

                IChromaticEffect effect = MovementRegistry.create(movementId, colorSpeed, moveSpeed, colors);

                ResourceLocation fontId = PhoenixChromaticCodes.id("code_" + (int) Character.toLowerCase(code));

                PhoenixChromaticCodes.LOGGER.info(
                        "Phoenix Chromatic: Registering §{} with effect {} on font {}", code, movementId, fontId);

                ChromaticAPI.registerEffect(code, fontId, effect);
            } catch (Exception e) {
                PhoenixChromaticCodes.LOGGER.error("Failed to register chromatic effect: " + entry, e);
            }
        }
    }

    public static void parseAndRegisterNamed(String[] configEntries) {
        if (configEntries == null || configEntries.length == 0) return;

        for (String entry : configEntries) {
            try {
                if (!entry.startsWith("[")) continue;
                int bracket = entry.indexOf(']');
                if (bracket < 2) continue;

                String name = entry.substring(1, bracket);
                if (bracket + 2 > entry.length()) continue;
                String rest = entry.substring(bracket + 2);

                String[] parts = rest.split(":");
                if (parts.length < 4) continue;

                float colorSpeed = Float.parseFloat(parts[0]);
                float moveSpeed = Float.parseFloat(parts[1]);
                String movementId = parts[2].toLowerCase();
                List<Integer> colors = resolveColors(parts[3].toLowerCase());

                // Same guard as above for named entries using the old outline movementId
                if (movementId.equals("outline")) {
                    PhoenixChromaticCodes.LOGGER.warn(
                            "Phoenix Chromatic: Named entry '{}' uses movementId 'outline', which has moved " +
                                    "to the [formatting] section. This entry has been ignored.",
                            entry);
                    continue;
                }

                IChromaticEffect effect = MovementRegistry.create(movementId, colorSpeed, moveSpeed, colors);

                String fontPath = "named_" + Math.abs(name.hashCode());
                ResourceLocation fontId = PhoenixChromaticCodes.id(fontPath);

                PhoenixChromaticCodes.LOGGER.info(
                        "Phoenix Chromatic: Registering §[{}] with effect {} on font {}", name, movementId, fontId);

                ChromaticAPI.registerNamedEffect(name, fontId, effect);
            } catch (Exception e) {
                PhoenixChromaticCodes.LOGGER.error("Failed to register named chromatic effect: " + entry, e);
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

    /**
     * Parses a string containing §[name] and §c style codes into a Component tree.
     */
    public static Component parseCustomEffects(String text) {
        MutableComponent root = Component.literal("");
        int i = 0;
        int len = text.length();
        StringBuilder plain = new StringBuilder();

        while (i < len) {
            char c = text.charAt(i);

            if (c == '§' && i + 1 < len) {
                if (plain.length() > 0) {
                    root.append(Component.literal(plain.toString()));
                    plain.setLength(0);
                }

                char next = Character.toLowerCase(text.charAt(i + 1));

                // Named bracket code: §[name]content
                if (next == '[') {
                    int closeIdx = text.indexOf(']', i + 2);
                    if (closeIdx != -1) {
                        String name = text.substring(i + 2, closeIdx);
                        i = closeIdx + 1;

                        int contentEnd = text.indexOf('§', i);
                        String content = contentEnd == -1 ? text.substring(i) : text.substring(i, contentEnd);
                        i = contentEnd == -1 ? len : contentEnd;

                        MutableComponent seg = Component.literal(content);
                        ResourceLocation font = ChromaticAPI.getFontForNamedCode(name);
                        if (font != null) {
                            seg = seg.withStyle(s -> s.withFont(font).withColor((TextColor) null));
                        } else if (ChromaticColors.NAMED_CUSTOM_FORMATTING
                                .containsKey(ChromaticAPI.normalizeNamedKey(name))) {
                                    int hex = ChromaticColors.NAMED_CUSTOM_FORMATTING
                                            .get(ChromaticAPI.normalizeNamedKey(name));
                                    seg = seg.withStyle(s -> s.withColor(hex));
                                } else {
                                    root.append(Component.literal("§[" + name + "]" + content));
                                    continue;
                                }
                        root.append(seg);
                        continue;
                    }
                }

                // Single-char code
                char code = next;
                i += 2;

                int contentEnd = text.indexOf('§', i);
                String content = contentEnd == -1 ? text.substring(i) : text.substring(i, contentEnd);
                i = contentEnd == -1 ? len : contentEnd;

                MutableComponent seg = Component.literal(content);
                ResourceLocation font = ChromaticAPI.getFontForCode(code);
                if (font != null) {
                    seg = seg.withStyle(s -> s.withFont(font).withColor((TextColor) null));
                } else if (CUSTOM_FORMATTING.containsKey(code)) {
                    int hex = CUSTOM_FORMATTING.get(code);
                    seg = seg.withStyle(s -> s.withColor(hex));
                } else {
                    root.append(Component.literal("§" + code + content));
                    continue;
                }
                root.append(seg);

            } else {
                plain.append(c);
                i++;
            }
        }

        if (plain.length() > 0) root.append(Component.literal(plain.toString()));
        return root;
    }
}
