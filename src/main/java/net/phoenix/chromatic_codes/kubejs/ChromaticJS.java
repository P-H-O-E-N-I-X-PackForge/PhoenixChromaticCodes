package net.phoenix.chromatic_codes.kubejs;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.chromatic_codes.api.ChromaticEffectsRegistry;
import net.phoenix.chromatic_codes.api.InlineEffects;
import net.phoenix.chromatic_codes.api.MovementRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The {@code Chromatic} global, available in every script type. */
public final class ChromaticJS {

    private ChromaticJS() {}

    /** Every movement id usable in {@code event.effect(...)} / inline definitions, sorted. */
    public static List<String> movements() {
        List<String> ids = new ArrayList<>(MovementRegistry.ids());
        Collections.sort(ids);
        return ids;
    }

    /**
     * Parses {@code §}-style text into a component: config-defined codes, {@code §[name]} codes and inline
     * {@code §[~...]} definitions. Codes you defined via {@code ChromaticEvents.register} only exist on clients, so
     * they aren't resolved here; use plain strings containing them (they're parsed when rendered) or
     * {@link #inline}.
     */
    public static Component text(String raw) {
        return ChromaticEffectsRegistry.parseCustomEffects(raw);
    }

    /**
     * Text with a one-off effect baked into its style, e.g. {@code Chromatic.inline('wave:FF0000,00FF00', 'Hi')}.
     * {@code spec} uses the inline syntax without brackets: {@code colors}, {@code movement},
     * {@code movement:colors} or {@code colorSpeed:moveSpeed:movement:colors}. Works regardless of the
     * {@code allowInlineCodes} setting since the script, not a player, wrote it.
     */
    public static MutableComponent inline(String spec, String text) {
        String body = spec.startsWith("~") ? spec.substring(1) : spec;
        ResourceLocation font = InlineEffects.parseDefinition(body);
        if (font == null) {
            throw new IllegalArgumentException("Invalid chromatic inline definition '" + spec +
                    "'. Movements: " + movements());
        }
        return Component.literal(text).withStyle(s -> s.withFont(font).withColor((TextColor) null));
    }
}
