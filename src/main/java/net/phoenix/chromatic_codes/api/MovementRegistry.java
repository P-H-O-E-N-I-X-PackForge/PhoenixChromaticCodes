package net.phoenix.chromatic_codes.api;

import net.phoenix.chromatic_codes.PhoenixChromaticCodes;
import net.phoenix.chromatic_codes.api.effects.DynamicEffect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovementRegistry {

    private static final Map<String, MovementFactory> REGISTRY = new HashMap<>();

    public static void register(String id, MovementFactory factory) {
        REGISTRY.put(id.toLowerCase(), factory);
    }

    // Must take 4 arguments: id, colorSpeed, moveSpeed, colors
    public static IChromaticEffect create(String id, float colorSpeed, float moveSpeed, List<Integer> colors) {
        // Force lowercase to avoid "Wave" vs "wave" issues
        MovementFactory factory = REGISTRY.get(id.toLowerCase());

        if (factory == null) {
            PhoenixChromaticCodes.LOGGER.error("Could not find movement type: " + id + ". Defaulting to static.");
            return new DynamicEffect(colors, colorSpeed, moveSpeed, "none");
        }
        return factory.build(colorSpeed, moveSpeed, colors);
    }

    @FunctionalInterface
    public interface MovementFactory {

        IChromaticEffect build(float colorSpeed, float moveSpeed, List<Integer> colors);
    }
}
