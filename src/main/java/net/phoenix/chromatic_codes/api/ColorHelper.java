package net.phoenix.chromatic_codes.api;

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
}