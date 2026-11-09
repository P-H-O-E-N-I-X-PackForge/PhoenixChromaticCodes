package net.phoenix.chromatic_codes.api;

import net.minecraft.resources.ResourceLocation;
import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Codes defined at runtime by scripts (KubeJS), as plain data that can be sent from server to client.
 * <p>
 * Effects are rendered client-side, so a server script can't register anything into the client's
 * {@link ChromaticAPI} directly. Instead the server keeps a {@link Definitions} snapshot, ships it to each client
 * (see {@code ChromaticNetwork}), and the client {@link #apply applies} it here. Applying again first undoes the
 * previous snapshot, which is what makes {@code /reload} work.
 * <p>
 * Effect codes are mapped onto inline fonts ({@link InlineEffects#define}) so no font resource is needed and no
 * resource reload is required. Anything a script overrides is restored on {@link #clear()}.
 * <p>
 * Only touch this from the client main thread.
 */
public final class ScriptedCodes {

    public record Fx(String key, boolean named, String movement, float colorSpeed, @Nullable Float moveSpeed,
                     List<Integer> colors, @Nullable String font) {}

    public record ColorDef(String key, boolean named, int rgb) {}

    public record OutlineDef(String key, boolean named) {}

    public record Definitions(List<Fx> effects, List<ColorDef> colors, List<OutlineDef> outlines,
                              @Nullable Integer outlineColor) {

        public static final Definitions EMPTY = new Definitions(List.of(), List.of(), List.of(), null);
    }

    // LIFO: each entry puts back exactly what one apply step replaced.
    private static final Deque<Runnable> UNDO = new ArrayDeque<>();

    private ScriptedCodes() {}

    // Two layers, rebuilt together so one reloading never clobbers the other:
    // - client: defined by client scripts (ChromaticEvents.clientRegister); survives joining/leaving servers
    // - server: synced from the server you're on (ChromaticEvents.register); dropped when you leave
    // Server wins where both define the same code; config codes are what's underneath.
    private static Definitions clientDefs = Definitions.EMPTY;
    private static Definitions serverDefs = Definitions.EMPTY;

    /** Server-synced layer. */
    public static synchronized void apply(Definitions defs) {
        serverDefs = defs == null ? Definitions.EMPTY : defs;
        rebuild();
    }

    /** Client-script layer: replaces whatever client scripts defined before. */
    public static synchronized void applyClient(Definitions defs) {
        clientDefs = defs == null ? Definitions.EMPTY : defs;
        rebuild();
    }

    /** Drops the server-synced layer (leaving a server); client-script codes stay. */
    public static synchronized void clear() {
        serverDefs = Definitions.EMPTY;
        rebuild();
    }

    public static synchronized void clearClient() {
        clientDefs = Definitions.EMPTY;
        rebuild();
    }

    private static void rebuild() {
        while (!UNDO.isEmpty()) UNDO.pop().run();
        applyLayer(clientDefs);
        applyLayer(serverDefs);
    }

    private static void applyLayer(Definitions defs) {
        for (Fx fx : defs.effects()) {
            ResourceLocation font = InlineEffects.define(fx.movement(), fx.colorSpeed(), fx.moveSpeed(), fx.colors());
            if (font == null) {
                PhoenixChromaticCodes.LOGGER.warn("Phoenix Chromatic: ignoring scripted effect '{}' (movement '{}')",
                        fx.key(), fx.movement());
                continue;
            }
            if (fx.font() != null) {
                ResourceLocation base = ChromaticAPI.getBaseFontByName(fx.font());
                if (base == null) {
                    PhoenixChromaticCodes.LOGGER.warn("Phoenix Chromatic: scripted effect '{}' names unknown font '{}'",
                            fx.key(), fx.font());
                    continue;
                }
                font = ChromaticAPI.composeFont(font, base);
            }
            String key = fx.key();
            if (fx.named()) {
                ResourceLocation prev = ChromaticAPI.aliasNamed(key, font);
                UNDO.push(() -> ChromaticAPI.restoreNamed(key, prev));
            } else {
                char c = key.charAt(0);
                ResourceLocation prev = ChromaticAPI.aliasCode(c, font);
                UNDO.push(() -> ChromaticAPI.restoreCode(c, prev));
            }
        }

        for (ColorDef color : defs.colors()) {
            String key = color.key();
            int rgb = color.rgb();
            if (color.named()) {
                String nk = ChromaticAPI.normalizeNamedKey(key);
                Integer prev = ChromaticColors.NAMED_CUSTOM_FORMATTING.put(nk, rgb);
                UNDO.push(() -> restore(ChromaticColors.NAMED_CUSTOM_FORMATTING, nk, prev));
            } else {
                char c = Character.toLowerCase(key.charAt(0));
                Integer prev = ChromaticColors.CUSTOM_FORMATTING.put(c, rgb);
                UNDO.push(() -> restore(ChromaticColors.CUSTOM_FORMATTING, c, prev));
            }
        }

        for (OutlineDef outline : defs.outlines()) {
            String key = outline.key();
            if (outline.named()) {
                boolean had = ChromaticAPI.isNamedOutlineCode(key);
                ChromaticAPI.registerNamedOutlineCode(key);
                if (!had) UNDO.push(() -> ChromaticAPI.unregisterNamedOutlineCode(key));
            } else {
                char c = key.charAt(0);
                boolean had = ChromaticAPI.isOutlineCode(c);
                ChromaticAPI.registerOutlineCode(c);
                if (!had) UNDO.push(() -> ChromaticAPI.unregisterOutlineCode(c));
            }
        }

        if (defs.outlineColor() != null) {
            int prev = ChromaticAPI.getOutlineColorValue();
            ChromaticAPI.setOutlineColorValue(defs.outlineColor());
            UNDO.push(() -> ChromaticAPI.setOutlineColorValue(prev));
        }
    }

    private static <K> void restore(java.util.Map<K, Integer> map, K key, @Nullable Integer prev) {
        if (prev == null) map.remove(key);
        else map.put(key, prev);
    }
}
