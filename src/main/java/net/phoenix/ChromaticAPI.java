package net.phoenix;

import net.minecraft.resources.ResourceLocation;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import java.util.HashMap;
import java.util.Map;

public class ChromaticAPI {

    // Maps a character (e.g., 'w') to the Logic (IChromaticEffect)
    private static final Map<Character, IChromaticEffect> EFFECTS = new HashMap<>();

    // Maps a Font ResourceLocation to the Logic (Used by the Renderer Mixin)
    private static final Map<ResourceLocation, IChromaticEffect> FONT_TO_EFFECT = new HashMap<>();

    // Maps a character to a Font (Used by the Decomposer Mixin)
    private static final Map<Character, ResourceLocation> CODE_TO_FONT = new HashMap<>();

    public static void registerEffect(char code, ResourceLocation font, IChromaticEffect effect) {
        char lower = Character.toLowerCase(code);
        EFFECTS.put(lower, effect);
        CODE_TO_FONT.put(lower, font);
        FONT_TO_EFFECT.put(font, effect);
    }

    public static IChromaticEffect getByFont(ResourceLocation font) {
        return FONT_TO_EFFECT.get(font);
    }

    public static ResourceLocation getFontForCode(char code) {
        return CODE_TO_FONT.get(Character.toLowerCase(code));
    }

    public static boolean isRegistered(char code) {
        return CODE_TO_FONT.containsKey(Character.toLowerCase(code));
    }
}
