package net.phoenix.chromatic_codes.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.ChromaticAPI;
import net.phoenix.chromatic_codes.config.ModConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ChromaticColors {

    public static final Map<Character, Integer> CUSTOM_FORMATTING = new HashMap<>();
    public static final Map<String, Integer> NAMED_CUSTOM_FORMATTING = new HashMap<>();
    public static final ThreadLocal<Character> LAST_CODE = ThreadLocal.withInitial(() -> ' ');
    public static final Logger LOGGER = LogManager.getLogger();

    public static void registerCustomColor(char code, int hex) {
        CUSTOM_FORMATTING.put(Character.toLowerCase(code), hex);
    }

    public static void registerNamedColor(String name, int hex) {
        NAMED_CUSTOM_FORMATTING.put(ChromaticAPI.normalizeNamedKey(name), hex);
    }

    public static void loadColorsFromConfig() {
        String[] colorDefinitions = ModConfig.INSTANCE.colors.customColors;

        for (String entry : colorDefinitions) {
            if (entry == null || !entry.contains(":")) continue;

            try {
                String[] split = entry.split(":");
                if (split.length == 2) {
                    char codeChar = split[0].charAt(0);
                    String hexPart = split[1].replace("#", "").trim();
                    int colorInt = Integer.parseInt(hexPart, 16);

                    registerCustomColor(codeChar, colorInt);
                    LOGGER.info("Registered custom color '§{}' as #{}", codeChar, hexPart);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to parse color config entry: {}", entry);
            }
        }
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

            // 1. Check if it's a Dynamic Effect (Wave/Shake/Gradient)
            ResourceLocation dynamicFont = ChromaticAPI.getFontForCode(code);
            if (dynamicFont != null) {
                segment = segment.withStyle(s -> s.withFont(dynamicFont));
            }
            // 2. Check if it's a Static Custom Color
            else if (CUSTOM_FORMATTING.containsKey(code)) {
                int color = CUSTOM_FORMATTING.get(code);
                segment = segment.withStyle(s -> s.withColor(color));
            }
            // 3. Fallback for vanilla
            else {
                root.append(Component.literal("§" + part));
                continue;
            }
            root.append(segment);
        }
        return root;
    }

    public static void init() {
        // --- Single-char colors ---
        String[] colorSettings = ModConfig.INSTANCE.colors.customColors;
        if (colorSettings != null) {
            for (String entry : colorSettings) {
                if (entry == null || !entry.contains(":")) continue;
                try {
                    String[] parts = entry.split(":", 2);
                    if (parts.length < 2) continue;

                    String codePart = parts[0].trim();
                    String hexPart = parts[1].replace("#", "").trim();

                    if (!codePart.isEmpty() && !hexPart.isEmpty()) {
                        char codeChar = codePart.charAt(0);
                        int colorInt = Integer.parseInt(hexPart, 16);
                        registerCustomColor(codeChar, colorInt);
                        LOGGER.info("Phoenix Colors: Mapping §{} to #{}", codeChar, hexPart);
                    }
                } catch (Exception e) {
                    LOGGER.error("Phoenix Core: Failed to parse color config entry '{}'", entry);
                }
            }
        }

        // --- Named bracket colors ---
        String[] namedColorSettings = ModConfig.INSTANCE.colors.namedColors;
        if (namedColorSettings != null) {
            for (String entry : namedColorSettings) {
                if (entry == null || entry.isEmpty()) continue;
                try {
                    // Format: "[name]:hex"
                    if (!entry.startsWith("[")) continue;
                    int bracket = entry.indexOf(']');
                    if (bracket < 2) continue;

                    String name = entry.substring(1, bracket);
                    // +1 for ']', +1 for ':'
                    if (bracket + 2 > entry.length()) continue;
                    String hexPart = entry.substring(bracket + 2).replace("#", "").trim();

                    int colorInt = Integer.parseInt(hexPart, 16);
                    registerNamedColor(name, colorInt);
                    LOGGER.info("Phoenix Colors: Mapping §[{}] to #{}", name, hexPart);
                } catch (Exception e) {
                    LOGGER.error("Phoenix Core: Failed to parse named color config entry '{}'", entry);
                }
            }
        }
    }
}
