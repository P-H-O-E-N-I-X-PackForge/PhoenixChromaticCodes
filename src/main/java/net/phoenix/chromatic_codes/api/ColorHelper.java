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

        // 1. Get current time in seconds
        double time = (System.currentTimeMillis() % 1000000L) / 1000.0;

        // 2. Calculate progress (0.0 to 1.0)
        // spread 0.02 means the gradient repeats every 50 pixels.
        double progress = (time * speed + (xPos * 0.02)) % 1.0;
        if (progress < 0) progress += 1.0;

        // 3. Map progress to color list
        int count = colors.size();
        double scaledProgress = progress * count;

        int index = (int) Math.floor(scaledProgress) % count;
        int nextIndex = (index + 1) % count;

        // 4. Calculate the interpolation factor
        float factor = (float) (scaledProgress - Math.floor(scaledProgress));

        return lerp(colors.get(index), colors.get(nextIndex), factor);
    }

    public static int getDiscordGradient(List<Integer> colors, float xPos) {
        if (colors.size() < 2) return colors.isEmpty() ? 0xFFFFFF : colors.get(0);

        // 80.0f makes short words pop while keeping long words smooth.
        float gradientLength = 80.0f;

        // Use modulo to create a repeating sawtooth wave
        float progress = (xPos / gradientLength) % 1.0f;
        if (progress < 0) progress += 1.0f;

        int count = colors.size();
        float scaledProgress = progress * (count - 1);
        int index = (int) scaledProgress;
        int nextIndex = Math.min(index + 1, count - 1);
        float factor = scaledProgress - index;

        return lerp(colors.get(index), colors.get(nextIndex), factor);
    }
}
