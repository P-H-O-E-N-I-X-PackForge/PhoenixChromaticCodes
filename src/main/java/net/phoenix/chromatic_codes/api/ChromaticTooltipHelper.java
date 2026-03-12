package net.phoenix.chromatic_codes.api;

import net.minecraft.network.chat.Style;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;

import java.util.function.UnaryOperator;

public class ChromaticTooltipHelper {

    /**
     * Makes the text shake violently.
     */
    public static final UnaryOperator<Style> SHAKE = style -> style.withFont(PhoenixChromaticCodes.id("shake"));

    /**
     * Makes the text bob up and down in a wave.
     */
    public static final UnaryOperator<Style> WAVE = style -> style.withFont(PhoenixChromaticCodes.id("wave"));

    /**
     * Combined effect: Rainbow + Wave
     */
    public static UnaryOperator<Style> rainbowWave(float speed) {
        return style -> {
            float time = (System.currentTimeMillis() % 10000) / 1000f * speed;
            int color = java.awt.Color.HSBtoRGB(time % 1.0f, 0.8f, 1.0f);
            return style.withColor(color).withFont(PhoenixChromaticCodes.id("wave"));
        };
    }

    public static final UnaryOperator<Style> GLOW = style -> style.withFont(PhoenixChromaticCodes.id("glow"));

    public static final UnaryOperator<Style> PULSE = style -> {
        float time = (System.currentTimeMillis() % 2000) / 2000f;
        float alpha = 0.5f + (float) Math.sin(time * Math.PI * 2) * 0.5f;
        int alphaInt = (int) (alpha * 255) << 24;
        // We use the font as a marker and the color's alpha for intensity
        return style.withFont(PhoenixChromaticCodes.id("glow"));
    };
}
