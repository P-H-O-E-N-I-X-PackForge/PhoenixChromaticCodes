package net.phoenix.chromatic_codes.api;

/**
 * A linear gradient defined in a font's own em space, the way a COLR table defines one: a start point, an end point
 * and color stops (clamped outside). Evaluated per vertex by the glyph renderer, so every glyph gets the same
 * gradient relative to its own position on the baseline, exactly like the color font it came from.
 */
public final class GlyphGradient {

    /** Vanilla draws text with its baseline this many GUI px below the text's top. */
    public static final float BASELINE = 7.0f;

    private final float x0, y0, dx, dy, invLenSq;
    private final float unitsPerPx;
    private final float[] offsets;
    private final int[] rgb;

    /**
     * @param x0,y0,x1,y1 gradient line in font units (y up, origin at the pen on the baseline)
     * @param emPx        the font's size in GUI px per em (the font provider's "size")
     * @param unitsPerEm  the font's units per em
     * @param offsets     ascending stop positions in 0..1
     * @param rgb         0xRRGGBB color per stop
     */
    public GlyphGradient(float x0, float y0, float x1, float y1, float emPx, float unitsPerEm, float[] offsets,
                         int[] rgb) {
        if (offsets.length != rgb.length || offsets.length == 0) throw new IllegalArgumentException("stops");
        this.x0 = x0;
        this.y0 = y0;
        this.dx = x1 - x0;
        this.dy = y1 - y0;
        this.invLenSq = 1.0f / (dx * dx + dy * dy);
        this.unitsPerPx = unitsPerEm / emPx;
        this.offsets = offsets.clone();
        this.rgb = rgb.clone();
    }

    /**
     * @param pxRight         GUI px right of the glyph's pen position
     * @param pxAboveBaseline GUI px above the baseline (negative below it)
     */
    public int colorAt(float pxRight, float pxAboveBaseline) {
        float ux = pxRight * unitsPerPx;
        float uy = pxAboveBaseline * unitsPerPx;
        float t = ((ux - x0) * dx + (uy - y0) * dy) * invLenSq;
        return sample(t);
    }

    int sample(float t) {
        int last = offsets.length - 1;
        if (t <= offsets[0]) return rgb[0];
        if (t >= offsets[last]) return rgb[last];
        int i = 1;
        while (offsets[i] < t) i++;
        float span = offsets[i] - offsets[i - 1];
        float f = span <= 0 ? 1.0f : (t - offsets[i - 1]) / span;
        int a = rgb[i - 1], b = rgb[i];
        int r = Math.round(((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * f);
        int g = Math.round(((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * f);
        int bl = Math.round((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * f);
        return (r << 16) | (g << 8) | bl;
    }
}
