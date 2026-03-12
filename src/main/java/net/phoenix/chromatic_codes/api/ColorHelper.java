package net.phoenix.chromatic_codes.api;

import java.util.List;

public class ColorHelper {

    public static int lerp(int col1, int col2, float factor) {
        int r1 = (col1 >> 16) & 0xFF;
        int g1 = (col1 >> 8) & 0xFF;
        int b1 = col1 & 0xFF;

        int r2 = (col2 >> 16) & 0xFF;
        int g2 = (col2 >> 8) & 0xFF;
        int b2 = col2 & 0xFF;

        int r = (int) (r1 + factor * (r2 - r1));
        int g = (int) (g1 + factor * (g2 - g1));
        int b = (int) (b1 + factor * (b2 - b1));

        return (r << 16) | (g << 8) | b;
    }
    public static int getGradientColor(List<Integer> colors, float speed, float xPos) {
        if (colors.isEmpty()) return 0xFFFFFF;
        if (colors.size() == 1) return colors.get(0);

        // Using a large modulo to prevent float precision issues over long play sessions
        double time = (System.currentTimeMillis() % 1000000L) / 1000.0;

        // xPos is the 'offset'. If xPos is 0, everyone is at the same point in the cycle.
        double progress = (time * speed + (xPos * 0.005f)) % 1.0;
        if (progress < 0) progress += 1.0;

        double sectionProgress = progress * (colors.size() - 1);
        int index = (int) sectionProgress;
        float factor = (float) (sectionProgress - index);

        return lerp(colors.get(index), colors.get(index + 1), factor);
    }
}
