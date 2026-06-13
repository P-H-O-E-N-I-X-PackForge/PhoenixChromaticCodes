package net.phoenix.chromatic_codes;

import net.minecraft.resources.ResourceLocation;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Public API surface for Chromatic Codes.
 *
 * Outline is now a pure formatting modifier — like bold or italic — rather than
 * a font-slot effect. It is tracked via thread-locals (OUTLINE_ACTIVE /
 * OUTLINE_COLOR) so it composes freely with every other color/movement code.
 *
 * Registration: call registerOutlineCode(char) or registerNamedOutlineCode(String)
 * during mod init. The outline color itself is NOT part of the code; it is set
 * via the formatting section of the config and stored in OUTLINE_COLOR_VALUE.
 *
 * Clearing: §r resets the outline flag just like it resets bold/italic.
 */
public class ChromaticAPI {

    // -----------------------------------------------------------------------
    // Effect registry (font-slot effects: wave, gradient, etc.)
    // -----------------------------------------------------------------------

    private static final Map<Character, ResourceLocation> CODE_TO_FONT = new HashMap<>();
    private static final Map<String, ResourceLocation> NAMED_TO_FONT = new HashMap<>();
    private static final Map<ResourceLocation, IChromaticEffect> FONT_TO_EFFECT = new HashMap<>();
    private static final Set<ResourceLocation> DECORATOR_FONTS = new HashSet<>();

    public static void registerEffect(char code, ResourceLocation fontId, IChromaticEffect effect) {
        CODE_TO_FONT.put(Character.toLowerCase(code), fontId);
        FONT_TO_EFFECT.put(fontId, effect);
        if (effect.isOutline()) DECORATOR_FONTS.add(fontId);
    }

    public static void registerNamedEffect(String name, ResourceLocation fontId, IChromaticEffect effect) {
        NAMED_TO_FONT.put(normalizeNamedKey(name), fontId);
        FONT_TO_EFFECT.put(fontId, effect);
        if (effect.isOutline()) DECORATOR_FONTS.add(fontId);
    }

    public static ResourceLocation getFontForCode(char code) {
        return CODE_TO_FONT.get(Character.toLowerCase(code));
    }

    public static ResourceLocation getFontForNamedCode(String name) {
        return NAMED_TO_FONT.get(normalizeNamedKey(name));
    }

    public static IChromaticEffect getByFont(ResourceLocation fontId) {
        return FONT_TO_EFFECT.get(fontId);
    }

    public static boolean isRegistered(char code) {
        return CODE_TO_FONT.containsKey(Character.toLowerCase(code)) ||
                OUTLINE_CODES.contains(Character.toLowerCase(code));
    }

    public static boolean isNamedRegistered(String name) {
        return NAMED_TO_FONT.containsKey(normalizeNamedKey(name)) ||
                NAMED_OUTLINE_CODES.contains(normalizeNamedKey(name));
    }

    public static boolean isDecoratorEffect(IChromaticEffect effect) {
        if (effect == null) return false;
        // Check if this effect is registered under a decorator font slot
        for (ResourceLocation font : DECORATOR_FONTS) {
            if (FONT_TO_EFFECT.get(font) == effect) return true;
        }
        return false;
    }

    public static Set<ResourceLocation> getRegisteredFonts() {
        return FONT_TO_EFFECT.keySet();
    }

    // -----------------------------------------------------------------------
    // Outline formatting registry
    // These chars/names toggle the outline flag — they own NO font slot.
    // -----------------------------------------------------------------------

    private static final Set<Character> OUTLINE_CODES = new HashSet<>();
    private static final Set<String> NAMED_OUTLINE_CODES = new HashSet<>();

    /** The single shared outline color, set from config at startup. */
    private static int OUTLINE_COLOR_VALUE = 0x000000;

    public static void registerOutlineCode(char code) {
        OUTLINE_CODES.add(Character.toLowerCase(code));
    }

    public static void registerNamedOutlineCode(String name) {
        NAMED_OUTLINE_CODES.add(normalizeNamedKey(name));
    }

    public static void setOutlineColorValue(int rgb) {
        OUTLINE_COLOR_VALUE = rgb;
    }

    public static int getOutlineColorValue() {
        return OUTLINE_COLOR_VALUE;
    }

    public static boolean isOutlineCode(char code) {
        return OUTLINE_CODES.contains(Character.toLowerCase(code));
    }

    public static boolean isNamedOutlineCode(String name) {
        // Accept both bare "outline" and "outline:hexcode" forms
        return NAMED_OUTLINE_CODES.contains(normalizeNamedKey(stripOutlineColor(name)));
    }

    /**
     * Parses a bracket-code name that may carry an inline color override.
     *
     * Format: "outline" → uses the config outline color
     * "outline:FF0000" → uses 0xFF0000 as the border color for this span
     *
     * Returns null if the name is not an outline code at all.
     * Returns an int[1] whose sole element is the resolved RGB color if it is.
     */
    public static int[] parseOutlineBracket(String name) {
        String base = stripOutlineColor(name);
        if (!NAMED_OUTLINE_CODES.contains(normalizeNamedKey(base))) return null;

        // Check for an inline color override: "outline:RRGGBB"
        int colon = name.indexOf(':');
        if (colon != -1) {
            String hexPart = name.substring(colon + 1).trim();
            try {
                int color = (int) Long.parseLong(hexPart.replace("#", ""), 16);
                return new int[] { color };
            } catch (NumberFormatException ignored) {
                // Malformed hex — fall through to config color
            }
        }
        return new int[] { OUTLINE_COLOR_VALUE };
    }

    /** Strips a trailing ":hexcode" suffix from a bracket-code name. */
    private static String stripOutlineColor(String name) {
        int colon = name.indexOf(':');
        return colon == -1 ? name : name.substring(0, colon);
    }

    // -----------------------------------------------------------------------
    // Per-glyph thread-locals (outline formatting state)
    // -----------------------------------------------------------------------

    /** True for the duration of a glyph that has outline formatting active. */
    private static final ThreadLocal<Boolean> OUTLINE_ACTIVE = ThreadLocal.withInitial(() -> false);

    /** The resolved outline color for the current glyph (RGB, no alpha). */
    private static final ThreadLocal<Integer> OUTLINE_COLOR = ThreadLocal.withInitial(() -> 0x000000);

    public static void setOutlineActive(boolean active) {
        OUTLINE_ACTIVE.set(active);
    }

    public static boolean isOutlineActive() {
        return OUTLINE_ACTIVE.get();
    }

    public static void setOutlineColor(int rgb) {
        OUTLINE_COLOR.set(rgb);
    }

    public static int getOutlineColor() {
        return OUTLINE_COLOR.get();
    }

    // -----------------------------------------------------------------------
    // Per-glyph thread-locals (effect rendering state)
    // -----------------------------------------------------------------------

    private static final ThreadLocal<IChromaticEffect> CURRENT_EFFECT = ThreadLocal.withInitial(() -> null);
    private static final ThreadLocal<IChromaticEffect> DECORATOR_EFFECT = ThreadLocal.withInitial(() -> null);
    private static final ThreadLocal<ResourceLocation> CURRENT_FONT_CONTEXT = ThreadLocal.withInitial(() -> null);
    private static final ThreadLocal<ResourceLocation> PENDING_PRIMARY_FONT = ThreadLocal.withInitial(() -> null);
    private static final ThreadLocal<Float> SEGMENT_START_X = ThreadLocal.withInitial(() -> 0.0f);

    // Tracks vanilla legacy bold formatting state dynamically through custom text meshes
    private static final ThreadLocal<Boolean> BOLD_CONTEXT = ThreadLocal.withInitial(() -> false);

    public static void setCurrentEffect(IChromaticEffect e) {
        CURRENT_EFFECT.set(e);
    }

    public static IChromaticEffect getCurrentEffect() {
        return CURRENT_EFFECT.get();
    }

    public static void setDecoratorEffect(IChromaticEffect e) {
        DECORATOR_EFFECT.set(e);
    }

    public static IChromaticEffect getDecoratorEffect() {
        return DECORATOR_EFFECT.get();
    }

    public static void setCurrentFontContext(ResourceLocation r) {
        CURRENT_FONT_CONTEXT.set(r);
    }

    public static ResourceLocation getCurrentFontContext() {
        return CURRENT_FONT_CONTEXT.get();
    }

    public static void setPendingPrimaryFont(ResourceLocation r) {
        PENDING_PRIMARY_FONT.set(r);
    }

    public static ResourceLocation getPendingPrimaryFont() {
        return PENDING_PRIMARY_FONT.get();
    }

    public static void setSegmentStartX(float x) {
        SEGMENT_START_X.set(x);
    }

    public static float getSegmentStartX() {
        return SEGMENT_START_X.get();
    }

    public static void setBoldContext(boolean active) {
        BOLD_CONTEXT.set(active);
    }

    public static boolean isBoldContext() {
        return BOLD_CONTEXT.get();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    public static String normalizeNamedKey(String name) {
        if (name == null) return "";
        return name; // case handling delegated to call sites via config flag
    }
}
