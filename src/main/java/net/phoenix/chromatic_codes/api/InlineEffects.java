package net.phoenix.chromatic_codes.api;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;
import net.phoenix.chromatic_codes.config.ModConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One-off effect definitions written directly in text, e.g. {@code &[~FF0000,0000FF]Hello}.
 * <p>
 * Registered effects are keyed by a font id that has to exist as a resource, so they can only be created at startup.
 * Inline definitions skip that: the whole definition is encoded <i>into</i> the font id's path
 * ({@code phoenix_chromatic_codes:inline/<movement>.<colorSpeed>.<moveSpeed>.<c1>-<c2>...}) and the effect is rebuilt
 * from the path on demand. The client's {@code Font} (and ModernUI's {@code TextLayoutProcessor}, when loaded) is
 * told to resolve every inline id to the default font set (see {@code MixinFont} /
 * {@code MixinTextLayoutProcessor}), so no resource reload is involved and the text keeps its definition when
 * copied around.
 * <p>
 * Accepted forms (the text between the brackets, after the {@code ~}):
 * <ul>
 * <li>{@code colors} - static gradient, e.g. {@code ~FF0000,0000FF} or {@code ~f00,00f} or {@code ~rainbow}</li>
 * <li>{@code movement} - a movement id on its own, with defaults, e.g. {@code ~size}</li>
 * <li>{@code movement:colors} - e.g. {@code ~wave:FF0000,00FF00}</li>
 * <li>{@code colorSpeed:moveSpeed:movement:colors} - same order as the config gradient lists</li>
 * </ul>
 */
public final class InlineEffects {

    public static final char MARKER = '~';

    /** Every inline font id resolves to this font set client-side; the id itself only carries the definition. */
    public static final ResourceLocation BASE_FONT = Style.DEFAULT_FONT;

    private static final String PATH_PREFIX = "inline/";
    private static final int MAX_COLORS = 16;
    private static final float MAX_SPEED = 32.0f;
    private static final float MAX_3D_LAYERS = 16.0f;
    private static final int CACHE_LIMIT = 512;

    // Same list ChromaticEffectsRegistry uses for the "rainbow" keyword.
    private static final List<Integer> RAINBOW = List.of(0xFF0000, 0xFFFF00, 0x00FF00, 0x00FFFF, 0x0000FF, 0xFF00FF,
            0xFF0000);

    // moveSpeed means different things per movement (scale, px gap, angle, layers...), so "1.0" isn't a sane default
    // for all of them.
    private static final Map<String, Float> DEFAULT_MOVE_SPEED = Map.of(
            "size", 1.5f,
            "spacing", 2.0f,
            "slant", 0.25f,
            "shear", 0.2f,
            "3d", 5.0f);

    // Both caches are pure memoization: entries can be dropped at any time and are rebuilt from the text / font id.
    private static final Map<String, Optional<ResourceLocation>> PARSE_CACHE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, Optional<IChromaticEffect>> EFFECT_CACHE = new ConcurrentHashMap<>();

    private InlineEffects() {}

    public static boolean isEnabled() {
        ModConfig cfg = ModConfig.INSTANCE;
        return cfg == null || cfg.colors.allowInlineCodes;
    }

    public static boolean isInlineName(String bracketContent) {
        return bracketContent != null && bracketContent.length() > 1 && bracketContent.charAt(0) == MARKER;
    }

    public static boolean isInlineFont(ResourceLocation font) {
        return font != null && font.getNamespace().equals(PhoenixChromaticCodes.MOD_ID) &&
                font.getPath().startsWith(PATH_PREFIX);
    }

    /** @return true if {@code bracketContent} (text between {@code [} and {@code ]}) is a valid inline definition. */
    public static boolean isValid(String bracketContent) {
        return fontFor(bracketContent) != null;
    }

    /**
     * @param bracketContent text between {@code [} and {@code ]}, including the leading {@code ~}
     * @return the font id carrying the definition, or null if it isn't an inline code, is malformed, or inline codes
     *         are disabled in the config
     */
    public static ResourceLocation fontFor(String bracketContent) {
        if (!isInlineName(bracketContent) || !isEnabled()) return null;
        if (PARSE_CACHE.size() > CACHE_LIMIT) PARSE_CACHE.clear();
        return PARSE_CACHE.computeIfAbsent(bracketContent, s -> Optional.ofNullable(parse(s.substring(1))))
                .orElse(null);
    }

    /** @return the effect encoded in an inline font id, or null if the id isn't a valid inline font. */
    public static IChromaticEffect effectFor(ResourceLocation font) {
        if (!isInlineFont(font)) return null;
        if (EFFECT_CACHE.size() > CACHE_LIMIT) EFFECT_CACHE.clear();
        return EFFECT_CACHE.computeIfAbsent(font, f -> Optional.ofNullable(decode(f))).orElse(null);
    }

    /**
     * Builds an inline font id from explicit parameters. Meant for trusted callers (KubeJS scripts, other mods) that
     * define effects themselves rather than players typing them, so unlike {@link #fontFor} this ignores
     * {@code allowInlineCodes}. Same limits as typed definitions: no "outline", at most 16 colors, speeds clamped.
     *
     * @param moveSpeed null uses the movement's default
     * @return the font id, or null if the movement is unknown/unsupported, the colors are empty or too many, or a
     *         speed isn't finite
     */
    public static ResourceLocation define(String movement, float colorSpeed, Float moveSpeed, List<Integer> colors) {
        if (movement == null) return null;
        String m = norm(movement);
        if (!isAllowedMovement(m)) return null;
        if (colors == null || colors.isEmpty() || colors.size() > MAX_COLORS) return null;
        if (!Float.isFinite(colorSpeed)) return null;
        float ms = moveSpeed != null ? moveSpeed : DEFAULT_MOVE_SPEED.getOrDefault(m, 1.0f);
        if (!Float.isFinite(ms)) return null;
        return encode(m, clampSpeed(colorSpeed), clampMove(m, ms), colors);
    }

    /** Like {@link #fontFor} for trusted callers: takes the definition without the leading {@code ~}. */
    public static ResourceLocation parseDefinition(String body) {
        return body == null ? null : parse(body);
    }

    private static ResourceLocation parse(String body) {
        String[] t = body.split(":", -1);

        float colorSpeed = 1.0f;
        Float moveSpeed = null;
        String movement = "none";
        String colorToken = null;

        switch (t.length) {
            case 1 -> {
                String s = norm(t[0]);
                if (MovementRegistry.has(s)) movement = s;
                else colorToken = s;
            }
            case 2 -> {
                movement = norm(t[0]);
                colorToken = norm(t[1]);
            }
            case 4 -> {
                Float cs = parseSpeed(t[0]);
                Float ms = parseSpeed(t[1]);
                if (cs == null || ms == null) return null;
                colorSpeed = cs;
                moveSpeed = ms;
                movement = norm(t[2]);
                colorToken = norm(t[3]);
            }
            default -> {
                return null;
            }
        }

        if (!isAllowedMovement(movement)) return null;

        List<Integer> colors = colorToken == null ? List.of(0xFFFFFF) : parseColors(colorToken);
        if (colors == null) return null;

        float ms = moveSpeed != null ? moveSpeed : DEFAULT_MOVE_SPEED.getOrDefault(movement, 1.0f);
        return encode(movement, colorSpeed, clampMove(movement, ms), colors);
    }

    private static boolean isAllowedMovement(String movement) {
        // "outline" is a decorator pass rather than a plain effect and isn't supported inline.
        return !movement.equals("outline") && MovementRegistry.has(movement);
    }

    private static String norm(String s) {
        return s.trim().toLowerCase(Locale.ROOT);
    }

    private static Float parseSpeed(String s) {
        try {
            float f = Float.parseFloat(s.trim());
            if (Float.isNaN(f) || Float.isInfinite(f)) return null;
            return clampSpeed(f);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static float clampSpeed(float f) {
        return Math.max(-MAX_SPEED, Math.min(MAX_SPEED, f));
    }

    // "3d" treats moveSpeed as a layer count and draws every glyph that many times, so it gets a much lower cap.
    private static float clampMove(String movement, float ms) {
        ms = clampSpeed(ms);
        return movement.equals("3d") ? Math.min(ms, MAX_3D_LAYERS) : ms;
    }

    private static List<Integer> parseColors(String token) {
        if (token.equals("rainbow")) return RAINBOW;

        String[] parts = token.split(",", -1);
        if (parts.length > MAX_COLORS) return null;

        List<Integer> out = new ArrayList<>(parts.length);
        for (String part : parts) {
            String h = part.trim();
            if (h.startsWith("#")) h = h.substring(1);
            if (h.length() == 3) {
                h = "" + h.charAt(0) + h.charAt(0) + h.charAt(1) + h.charAt(1) + h.charAt(2) + h.charAt(2);
            }
            if (h.length() != 6) return null;
            for (int i = 0; i < 6; i++) {
                if (Character.digit(h.charAt(i), 16) < 0) return null;
            }
            out.add(Integer.parseInt(h, 16));
        }
        return out;
    }

    private static ResourceLocation encode(String movement, float colorSpeed, float moveSpeed, List<Integer> colors) {
        StringBuilder sb = new StringBuilder(PATH_PREFIX)
                .append(movement).append('.')
                .append(Integer.toHexString(Float.floatToIntBits(colorSpeed))).append('.')
                .append(Integer.toHexString(Float.floatToIntBits(moveSpeed))).append('.');
        for (int i = 0; i < colors.size(); i++) {
            if (i > 0) sb.append('-');
            sb.append(String.format(Locale.ROOT, "%06x", colors.get(i) & 0xFFFFFF));
        }
        try {
            return ResourceLocation.fromNamespaceAndPath(PhoenixChromaticCodes.MOD_ID, sb.toString());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static IChromaticEffect decode(ResourceLocation font) {
        try {
            String[] a = font.getPath().substring(PATH_PREFIX.length()).split("\\.", -1);
            if (a.length != 4) return null;

            String movement = a[0];
            if (!isAllowedMovement(movement)) return null;

            float colorSpeed = Float.intBitsToFloat(Integer.parseUnsignedInt(a[1], 16));
            float moveSpeed = Float.intBitsToFloat(Integer.parseUnsignedInt(a[2], 16));
            if (!Float.isFinite(colorSpeed) || !Float.isFinite(moveSpeed)) return null;

            String[] hex = a[3].split("-", -1);
            if (hex.length == 0 || hex.length > MAX_COLORS) return null;
            List<Integer> colors = new ArrayList<>(hex.length);
            for (String h : hex) colors.add(Integer.parseInt(h, 16) & 0xFFFFFF);

            // Clamp again: the id is only ever produced by encode(), but the limits are cheap insurance.
            return MovementRegistry.create(movement, clampSpeed(colorSpeed), clampMove(movement, moveSpeed), colors);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
