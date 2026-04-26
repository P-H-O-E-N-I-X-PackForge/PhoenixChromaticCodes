package net.phoenix;

import net.minecraft.resources.ResourceLocation;
import net.phoenix.chromatic_codes.api.IChromaticEffect;
import net.phoenix.chromatic_codes.config.ModConfig;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ChromaticAPI {

    private static final Map<Character, IChromaticEffect> EFFECTS = new HashMap<>();
    private static final Map<ResourceLocation, IChromaticEffect> FONT_TO_EFFECT = new HashMap<>();
    private static final Map<Character, ResourceLocation> CODE_TO_FONT = new HashMap<>();

    // Named bracket codes — key is already normalized via normalizeNamedKey()
    private static final Map<String, IChromaticEffect> NAMED_EFFECTS = new HashMap<>();
    private static final Map<String, ResourceLocation> NAMED_CODE_TO_FONT = new HashMap<>();

    // The Bridge: stores the effect being rendered on the current thread
    private static final ThreadLocal<IChromaticEffect> CURRENT_EFFECT = new ThreadLocal<>();

    /**
     * Normalizes a named code key according to the current case-sensitivity setting.
     * All registration and lookup must go through this so the behaviour is consistent.
     */
    public static String normalizeNamedKey(String name) {
        if (ModConfig.INSTANCE != null && !ModConfig.INSTANCE.colors.namedCodesCaseSensitive) {
            return name.toLowerCase();
        }
        return name; // case-sensitive — keep as-is
    }

    public static void setCurrentEffect(IChromaticEffect effect) {
        CURRENT_EFFECT.set(effect);
    }

    public static IChromaticEffect getCurrentEffect() {
        return CURRENT_EFFECT.get();
    }

    public static void registerEffect(char code, ResourceLocation font, IChromaticEffect effect) {
        char lower = Character.toLowerCase(code);
        EFFECTS.put(lower, effect);
        CODE_TO_FONT.put(lower, font);
        FONT_TO_EFFECT.put(font, effect);
    }

    public static void registerNamedEffect(String name, ResourceLocation font, IChromaticEffect effect) {
        String key = normalizeNamedKey(name);
        NAMED_EFFECTS.put(key, effect);
        NAMED_CODE_TO_FONT.put(key, font);
        FONT_TO_EFFECT.put(font, effect);
    }

    public static IChromaticEffect getByFont(ResourceLocation font) {
        return FONT_TO_EFFECT.get(font);
    }

    public static ResourceLocation getFontForCode(char code) {
        return CODE_TO_FONT.get(Character.toLowerCase(code));
    }

    public static ResourceLocation getFontForNamedCode(String name) {
        return NAMED_CODE_TO_FONT.get(normalizeNamedKey(name));
    }

    public static boolean isRegistered(char code) {
        return CODE_TO_FONT.containsKey(Character.toLowerCase(code));
    }

    public static boolean isNamedRegistered(String name) {
        return NAMED_CODE_TO_FONT.containsKey(normalizeNamedKey(name));
    }

    /**
     * Returns all font ResourceLocations that have been registered via registerEffect()
     * or registerNamedEffect(). Used by ChromaticDynamicPack to know which font JSONs
     * to generate at runtime.
     */
    public static Collection<ResourceLocation> getRegisteredFonts() {
        return Collections.unmodifiableCollection(FONT_TO_EFFECT.keySet());
    }
}
