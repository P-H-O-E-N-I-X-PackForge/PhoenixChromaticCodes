package net.phoenix.chromatic_codes;

import net.minecraft.resources.ResourceLocation;
import net.phoenix.chromatic_codes.api.GlyphGradient;
import net.phoenix.chromatic_codes.api.IChromaticEffect;
import net.phoenix.chromatic_codes.api.effects.GradientLayerEffect;
import net.phoenix.chromatic_codes.api.InlineEffects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChromaticAPI {

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
        IChromaticEffect effect = FONT_TO_EFFECT.get(fontId);
        if (effect == null && InlineEffects.isInlineFont(fontId)) return InlineEffects.effectFor(fontId);
        if (effect == null) {
            ResourceLocation[] parts = compositeParts(fontId);
            if (parts != null) return layeredEffect(fontId, parts);
        }
        return effect;
    }

    public static ResourceLocation getFontForInlineCode(String name) {
        return InlineEffects.fontFor(name);
    }

    public static boolean isInlineCode(String name) {
        return InlineEffects.isValid(name);
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

        for (ResourceLocation font : DECORATOR_FONTS) {
            if (FONT_TO_EFFECT.get(font) == effect) return true;
        }
        return false;
    }

    /**
     * Points a single-char code at a font id that already resolves to an effect through {@link #getByFont} (an inline
     * font, see {@link InlineEffects#define}), without registering an effect of its own. Used for codes defined at
     * runtime (KubeJS), which can't add font resources after the resource reload.
     *
     * @return the font this code pointed at before, or null; pass it to {@link #restoreCode} to undo.
     */
    public static ResourceLocation aliasCode(char code, ResourceLocation fontId) {
        return CODE_TO_FONT.put(Character.toLowerCase(code), fontId);
    }

    /** Named-code counterpart of {@link #aliasCode}. */
    public static ResourceLocation aliasNamed(String name, ResourceLocation fontId) {
        return NAMED_TO_FONT.put(normalizeNamedKey(name), fontId);
    }

    /** Puts a code back to what {@link #aliasCode} returned (null removes the mapping). */
    public static void restoreCode(char code, ResourceLocation previous) {
        char c = Character.toLowerCase(code);
        if (previous == null) CODE_TO_FONT.remove(c);
        else CODE_TO_FONT.put(c, previous);
    }

    /** Named-code counterpart of {@link #restoreCode}. */
    public static void restoreNamed(String name, ResourceLocation previous) {
        String key = normalizeNamedKey(name);
        if (previous == null) NAMED_TO_FONT.remove(key);
        else NAMED_TO_FONT.put(key, previous);
    }

    public static void unregisterOutlineCode(char code) {
        OUTLINE_CODES.remove(Character.toLowerCase(code));
    }

    public static void unregisterNamedOutlineCode(String name) {
        NAMED_OUTLINE_CODES.remove(normalizeNamedKey(name));
    }

    // ---- Layering: an effect on top of a resource font -------------------------------------------------------
    // A style has exactly one font id and the effect is looked up from it, so "wave on phoenix_display" can't be two
    // ids. Instead the pair is packed into one composite id:
    //     phoenix_chromatic_codes:with/<baseFontPath>/<effectFontPath>
    // getByFont() on it returns the effect part's effect; renderFont() maps it to the base font so glyphs come from
    // the real font. Base fonts must live in this mod's namespace (PhoenixFonts does).
    private static final String COMPOSITE_PREFIX = "with/";
    private static final Map<ResourceLocation, GradientLayerEffect> LAYERED = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Effect for a composite font. Normally that's just the effect part's effect. If the base font brings its own
     * gradient (display_color), the effect's movement is kept but its colors are replaced by the font's gradient.
     */
    private static IChromaticEffect layeredEffect(ResourceLocation fontId, ResourceLocation[] parts) {
        IChromaticEffect movement = getByFont(parts[0]);
        if (movement == null) return null;
        IChromaticEffect baseEffect = FONT_TO_EFFECT.get(parts[1]);
        GlyphGradient gradient = baseEffect == null ? null : baseEffect.getGlyphGradient();
        if (gradient == null) return movement;
        GradientLayerEffect cached = LAYERED.get(fontId);
        // Same instance every call (the renderer compares effects by identity), rebuilt if either part was re-registered
        if (cached == null || cached.movement() != movement || cached.gradient() != gradient) {
            cached = new GradientLayerEffect(movement, gradient);
            LAYERED.put(fontId, cached);
        }
        return cached;
    }
    private static final Map<ResourceLocation, ResourceLocation[]> COMPOSITES = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, ResourceLocation> BASE_FONT_NAMES = new HashMap<>();
    private static final Map<ResourceLocation, java.util.function.Supplier<Float>> BASE_FONT_Y_OFFSETS = new HashMap<>();

    /** Short names of fonts usable after '@' in config lines / inline codes / scripts (e.g. "display"). */
    public static Set<String> getBaseFontNames() {
        return java.util.Collections.unmodifiableSet(BASE_FONT_NAMES.keySet());
    }

    public static ResourceLocation getBaseFontByName(String name) {
        return name == null ? null : BASE_FONT_NAMES.get(normalizeNamedKey(name.trim()));
    }

    public static boolean isCompositeFont(ResourceLocation fontId) {
        return fontId != null && fontId.getNamespace().equals(PhoenixChromaticCodes.MOD_ID) &&
                fontId.getPath().startsWith(COMPOSITE_PREFIX);
    }

    /** @return {effectFont, baseFont} for a composite id, or null if it isn't one (or names an unknown base). */
    private static ResourceLocation[] compositeParts(ResourceLocation fontId) {
        if (!isCompositeFont(fontId)) return null;
        ResourceLocation[] known = COMPOSITES.get(fontId);
        if (known != null) return known;
        String rest = fontId.getPath().substring(COMPOSITE_PREFIX.length());
        int slash = rest.indexOf('/');
        if (slash <= 0 || slash == rest.length() - 1) return null;
        ResourceLocation base = PhoenixChromaticCodes.id(rest.substring(0, slash));
        ResourceLocation effect = PhoenixChromaticCodes.id(rest.substring(slash + 1));
        if (!RESOURCE_FONTS.contains(base)) return null;
        ResourceLocation[] parts = { effect, base };
        COMPOSITES.put(fontId, parts);
        return parts;
    }

    /**
     * Puts an effect font and a base font into one id. If either isn't in this mod's namespace (or the base isn't a
     * registered resource font) there's nothing to layer and the effect font is returned unchanged.
     */
    public static ResourceLocation composeFont(ResourceLocation effectFont, ResourceLocation baseFont) {
        String ns = PhoenixChromaticCodes.MOD_ID;
        if (!baseFont.getNamespace().equals(ns) || !effectFont.getNamespace().equals(ns) ||
                !RESOURCE_FONTS.contains(baseFont)) {
            return effectFont;
        }
        ResourceLocation id = PhoenixChromaticCodes.id(COMPOSITE_PREFIX + baseFont.getPath() + "/" + effectFont.getPath());
        COMPOSITES.putIfAbsent(id, new ResourceLocation[] { effectFont, baseFont });
        return id;
    }

    private static ResourceLocation basePartOf(ResourceLocation fontId) {
        ResourceLocation[] parts = compositeParts(fontId);
        if (parts != null) return parts[1];
        return RESOURCE_FONTS.contains(fontId) ? fontId : null;
    }

    private static ResourceLocation effectPartOf(ResourceLocation fontId) {
        ResourceLocation[] parts = compositeParts(fontId);
        if (parts != null) return parts[0];
        if (fontId == null || RESOURCE_FONTS.contains(fontId)) return null;   // a base font's own effect isn't a layer
        return fontId.getNamespace().equals(PhoenixChromaticCodes.MOD_ID) && getByFont(fontId) != null ? fontId : null;
    }

    /**
     * The font a style should end up with when a code switching to {@code incoming} is applied while the text is
     * currently in {@code current}. Whichever of effect / base font the new code provides replaces that layer; the
     * other layer carries over. So &[display] then &[wave] (or the reverse) gives wave drawn with the display font.
     * A vanilla color code resets the font entirely, as before.
     */
    public static ResourceLocation mergeFont(ResourceLocation current, ResourceLocation incoming) {
        ResourceLocation effect = effectPartOf(incoming);
        if (effect == null) effect = effectPartOf(current);
        ResourceLocation base = basePartOf(incoming);
        if (base == null) base = basePartOf(current);
        if (effect == null || base == null) return incoming;
        return composeFont(effect, base);
    }

    // A base font with a built-in effect (display_color's gradient) can name a plain sibling, which a vanilla color
    // code switches to, since "make this red" can't coexist with a baked-in gradient.
    private static final Map<ResourceLocation, ResourceLocation> PLAIN_VARIANTS = new HashMap<>();

    public static void setPlainVariant(ResourceLocation baseFont, ResourceLocation plainFont) {
        PLAIN_VARIANTS.put(baseFont, plainFont);
    }

    /**
     * The font to use after a vanilla color code (&c etc.): the current base font survives (its plain variant if it
     * has one), but any effect layer is dropped so the color applies. Anything that isn't one of our base fonts goes
     * back to the default font, as before.
     */
    public static ResourceLocation fontAfterColor(ResourceLocation current) {
        ResourceLocation base = basePartOf(current);
        if (base == null) return net.minecraft.network.chat.Style.DEFAULT_FONT;
        return PLAIN_VARIANTS.getOrDefault(base, base);
    }

    /** Which font set glyphs for this style font are drawn from (inline ids -> default, composites -> their base). */
    public static ResourceLocation renderFont(ResourceLocation fontId) {
        if (InlineEffects.isInlineFont(fontId)) return InlineEffects.BASE_FONT;
        ResourceLocation[] parts = compositeParts(fontId);
        return parts != null ? parts[1] : fontId;
    }

    /** Vertical correction (GUI px, positive = down) applied to every glyph drawn with this base font. */
    public static void setBaseFontYOffset(ResourceLocation baseFont, java.util.function.Supplier<Float> offset) {
        BASE_FONT_Y_OFFSETS.put(baseFont, offset);
    }

    public static float getBaseFontYOffset(ResourceLocation fontId) {
        ResourceLocation base = basePartOf(fontId);
        if (base == null) return 0f;
        java.util.function.Supplier<Float> supplier = BASE_FONT_Y_OFFSETS.get(base);
        Float value = supplier == null ? null : supplier.get();
        return value == null ? 0f : value;
    }

    // Fonts that exist as real font JSON files in a resource pack (as opposed to the generated stand-ins that
    // ChromaticDynamicPack serves). The dynamic pack must not shadow these.
    private static final Set<ResourceLocation> RESOURCE_FONTS = new HashSet<>();

    /**
     * Registers a named code ({@code &[name]}) for a font that ships as a real resource. {@code effect} may be null for
     * a plain font switch (text color is left alone); pass one to force a color or run per-glyph logic.
     */
    public static void registerResourceFontNamed(String name, ResourceLocation fontId, IChromaticEffect effect) {
        NAMED_TO_FONT.put(normalizeNamedKey(name), fontId);
        BASE_FONT_NAMES.put(normalizeNamedKey(name), fontId);
        RESOURCE_FONTS.add(fontId);
        if (effect != null) FONT_TO_EFFECT.put(fontId, effect);
    }

    /** Single-character counterpart of {@link #registerResourceFontNamed}. */
    public static void registerResourceFontCode(char code, ResourceLocation fontId, IChromaticEffect effect) {
        CODE_TO_FONT.put(Character.toLowerCase(code), fontId);
        RESOURCE_FONTS.add(fontId);
        if (effect != null) FONT_TO_EFFECT.put(fontId, effect);
    }

    public static boolean isResourceFont(ResourceLocation fontId) {
        return RESOURCE_FONTS.contains(fontId);
    }

    public static Set<ResourceLocation> getRegisteredFonts() {
        return FONT_TO_EFFECT.keySet();
    }

    private static final Set<Character> OUTLINE_CODES = new HashSet<>();
    private static final Set<String> NAMED_OUTLINE_CODES = new HashSet<>();

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
        return NAMED_OUTLINE_CODES.contains(normalizeNamedKey(stripOutlineColor(name)));
    }

    public static int[] parseOutlineBracket(String name) {
        String base = stripOutlineColor(name);
        if (!NAMED_OUTLINE_CODES.contains(normalizeNamedKey(base))) return null;

        int colon = name.indexOf(':');
        if (colon != -1) {
            String hexPart = name.substring(colon + 1).trim();
            try {
                int color = (int) Long.parseLong(hexPart.replace("#", ""), 16);
                return new int[] { color };
            } catch (NumberFormatException ignored) {

            }
        }
        return new int[] { OUTLINE_COLOR_VALUE };
    }

    private static String stripOutlineColor(String name) {
        int colon = name.indexOf(':');
        return colon == -1 ? name : name.substring(0, colon);
    }

    private static final ThreadLocal<Boolean> OUTLINE_ACTIVE = ThreadLocal.withInitial(() -> false);

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

    private static final ThreadLocal<IChromaticEffect> CURRENT_EFFECT = ThreadLocal.withInitial(() -> null);
    private static final ThreadLocal<IChromaticEffect> DECORATOR_EFFECT = ThreadLocal.withInitial(() -> null);
    private static final ThreadLocal<ResourceLocation> CURRENT_FONT_CONTEXT = ThreadLocal.withInitial(() -> null);
    private static final ThreadLocal<ResourceLocation> PENDING_PRIMARY_FONT = ThreadLocal.withInitial(() -> null);
    private static final ThreadLocal<Float> SEGMENT_START_X = ThreadLocal.withInitial(() -> 0.0f);

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

    public static String normalizeNamedKey(String name) {
        if (name == null) return "";
        return name;
    }
}
