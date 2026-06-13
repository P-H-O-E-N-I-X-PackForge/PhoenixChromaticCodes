package net.phoenix.chromatic_codes.api.effects;

import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.List;
import java.util.Random;

public class ChromaGlitchEffect implements IChromaticEffect {

    private final List<Integer> colors;
    private final Random rand = new Random();

    public ChromaGlitchEffect(List<Integer> colors) {
        this.colors = colors;
    }

    @Override
    public int getRenderColor(int originalColor, float x, float y) {
        if (colors == null || colors.isEmpty()) return 0xFF0055; // Cyberpunk pink default
        long timeSeed = System.currentTimeMillis() / 150; // Shift colors abruptly every 150ms
        int index = (int) ((timeSeed + (int) x) % colors.size());
        return colors.get(Math.abs(index));
    }

    @Override
    public float getXOffset(float x, float y) {
        long timeWindow = System.currentTimeMillis() / 200; // Determine glitch window interval
        // Create an unstable shaking artifact pop on random intervals
        if ((timeWindow % 7 == 0 || timeWindow % 13 == 0) && rand.nextFloat() > 0.4f) {
            return (rand.nextFloat() - 0.5f) * 3.5f; // Hard horizontal snap snap
        }
        return 0;
    }

    @Override
    public float getScale(float x, float y) {
        return 1.0f;
    }

    @Override
    public float getScaleX(float x, float y) {
        return 1.0f;
    }

    @Override
    public float getScaleY(float x, float y) {
        return 1.0f;
    }

    @Override
    public float getYOffset(float x, float y) {
        return 0;
    }
}
