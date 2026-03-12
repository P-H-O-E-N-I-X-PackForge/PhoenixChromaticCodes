package net.phoenix.chromatic_codes.api;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.ChromaticAPI;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;
import net.phoenix.chromatic_codes.config.ModConfig;

import java.util.ArrayList;
import java.util.List;

public class ChromaticEffectsRegistry {

    public static void init() {
        // Clear old mappings if re-initializing (useful for config reloads)
        parseAndRegister(ModConfig.INSTANCE.colors.customGradients);
    }

    public static void parseAndRegister(String[] configEntries) {
        if (configEntries == null) return;

        for (String entry : configEntries) {
            try {
                // Expected format: "char:speed:movementType:hex1,hex2..."
                String[] parts = entry.split(":");
                if (parts.length < 4) continue;

                char code = parts[0].charAt(0);
                float speed = Float.parseFloat(parts[1]);
                String movementType = parts[2].toLowerCase();
                String colorData = parts[3].toLowerCase();

                List<Integer> colors = new ArrayList<>();
                if (colorData.equals("rainbow")) {
                    colors = getRainbowColors();
                } else {
                    for (String hex : colorData.split(",")) {
                        colors.add(Integer.parseInt(hex.replace("#", "").trim(), 16));
                    }
                }

                final List<Integer> finalColors = colors;

                // Create a Font ID for this specific code (e.g., phoenix_chromatic_codes:code_w)
                // This is what the MixinStringDecomposer will look for.
                ResourceLocation fontId = PhoenixChromaticCodes.id("code_" + Character.toLowerCase(code));

                // Matches the new ChromaticAPI.registerEffect(char, ResourceLocation, IChromaticEffect)
                ChromaticAPI.registerEffect(code, fontId, new IChromaticEffect() {

                    @Override
                    public int getRenderColor(int originalColor, float x, float y) {
                        return getGradientColor(finalColors, speed, System.currentTimeMillis(), x);
                    }

                    @Override
                    public Style apply(Style style, float x, float y) {
                        // Apply movement first
                        Style movedStyle = applyMovement(style, movementType);
                        // Then apply the color logic from getRenderColor
                        return movedStyle.withColor(getRenderColor(0, x, y));
                    }
                });

            } catch (Exception e) {
                PhoenixChromaticCodes.LOGGER.error("Error parsing chromatic config entry [{}]: {}", entry,
                        e.getMessage());
            }
        }
    }

    private static List<Integer> getRainbowColors() {
        List<Integer> rainbow = new ArrayList<>();
        rainbow.add(0xFF0000); // Red
        rainbow.add(0xFFFF00); // Yellow
        rainbow.add(0x00FF00); // Green
        rainbow.add(0x00FFFF); // Cyan
        rainbow.add(0x0000FF); // Blue
        rainbow.add(0xFF00FF); // Magenta
        rainbow.add(0xFF0000); // Loop back
        return rainbow;
    }

    private static int getGradientColor(List<Integer> colors, float speed, long time, float xPos) {
        if (colors.isEmpty()) return 0xFFFFFF;
        if (colors.size() == 1) return colors.get(0);

        // Adjust 0.005f to change how fast the gradient "stretches" across words
        double progress = ((time * speed / 1000.0) + (xPos * 0.005f)) % 1.0;

        double sectionProgress = progress * (colors.size() - 1);
        int index = (int) sectionProgress;
        float factor = (float) (sectionProgress - index);

        return lerp(colors.get(index), colors.get(index + 1), factor);
    }

    private static Style applyMovement(Style style, String type) {
        if (style.getFont().getPath().equals("default")) return style;

        ResourceLocation movementId = new ResourceLocation(
                type.contains(":") ? type : "phoenix_chromatic_codes:" + type);
        IChromaticEffect effect = ChromaticAPI.getByFont(movementId);

        if (effect != null) {
            float time = System.currentTimeMillis();
            return style.withFont(movementId);
        }

        return style;
    }

    private static int lerp(int c1, int c2, float f) {
        int r = (int) (((c1 >> 16) & 0xFF) * (1 - f) + ((c2 >> 16) & 0xFF) * f);
        int g = (int) (((c1 >> 8) & 0xFF) * (1 - f) + ((c2 >> 8) & 0xFF) * f);
        int b = (int) ((c1 & 0xFF) * (1 - f) + (c2 & 0xFF) * f);
        return (r << 16) | (g << 8) | b;
    }
}
