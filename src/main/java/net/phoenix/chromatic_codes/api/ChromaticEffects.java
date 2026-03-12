package net.phoenix.chromatic_codes.api;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ChromaticEffects {

    private static final Map<ResourceLocation, IChromaticEffect> REGISTRY = new HashMap<>();

    public static void register(ResourceLocation id, IChromaticEffect effect) {
        REGISTRY.put(id, effect);
    }

    public static IChromaticEffect get(ResourceLocation id) {
        return REGISTRY.get(id);
    }
}
