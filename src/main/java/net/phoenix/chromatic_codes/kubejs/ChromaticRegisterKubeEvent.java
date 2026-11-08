package net.phoenix.chromatic_codes.kubejs;

import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.InlineEffects;
import net.phoenix.chromatic_codes.api.MovementRegistry;
import net.phoenix.chromatic_codes.api.ScriptedCodes.ColorDef;
import net.phoenix.chromatic_codes.api.ScriptedCodes.Definitions;
import net.phoenix.chromatic_codes.api.ScriptedCodes.Fx;
import net.phoenix.chromatic_codes.api.ScriptedCodes.OutlineDef;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.typings.Info;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@code ChromaticEvents.register(event => { ... })}. Collects definitions only; they're validated here (so mistakes
 * show up as script errors with a line number) and sent to clients afterwards. Later definitions of the same key win.
 */
public class ChromaticRegisterKubeEvent implements KubeEvent {

    private static final List<Integer> RAINBOW = List.of(0xFF0000, 0xFFFF00, 0x00FF00, 0x00FFFF, 0x0000FF, 0xFF00FF,
            0xFF0000);

    private final List<Fx> effects = new ArrayList<>();
    private final List<ColorDef> colors = new ArrayList<>();
    private final List<OutlineDef> outlines = new ArrayList<>();
    private Integer outlineColor;

    @Info("""
            Defines a gradient/effect code.
            `key` is a single character (`'q'`, used as `&q`) or a bracketed name (`'[ember]'`, used as `&[ember]`).
            `spec` is an object: `{ movement, colors, colorSpeed, moveSpeed }`.
            - `colors`: array of '#RRGGBB' / 'RRGGBB' / '#RGB' strings or numbers (1-16), or the string 'rainbow'
            - `movement`: see `Chromatic.movements()` (default 'none'; 'outline' isn't allowed, use `outline()`)
            - `colorSpeed` (default 1) and `moveSpeed` (default depends on the movement, e.g. size 1.5)
            - `font` (optional): a bundled font to draw with, see `Chromatic.fonts()` (e.g. 'display')
            Same meanings as the config's gradient lists.
            """)
    public void effect(String key, Object spec) {
        Key k = parseKey(key);
        if (!(spec instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("effect('" + key + "', spec): spec must be an object like " +
                    "{ movement: 'wave', colors: ['#ff0000', '#0000ff'] }");
        }

        String movement = map.get("movement") == null ? "none" : String.valueOf(map.get("movement")).toLowerCase();
        List<Integer> cols = parseColors(map.get("colors"));
        float colorSpeed = map.get("colorSpeed") == null ? 1.0f : number(map.get("colorSpeed"), "colorSpeed");
        Float moveSpeed = map.get("moveSpeed") == null ? null : number(map.get("moveSpeed"), "moveSpeed");

        if (InlineEffects.define(movement, colorSpeed, moveSpeed, cols) == null) {
            String why = movement.equals("outline") ? "'outline' isn't an effect here, use event.outline(key)" :
                    !MovementRegistry.has(movement) ? "unknown movement '" + movement + "' (valid: " +
                            sorted() + ")" :
                            "colors must have 1-16 entries and speeds must be finite numbers";
            throw new IllegalArgumentException("Invalid chromatic effect '" + key + "': " + why);
        }
        String font = map.get("font") == null ? null : String.valueOf(map.get("font")).trim();
        if (font != null && ChromaticAPI.getBaseFontByName(font) == null) {
            throw new IllegalArgumentException("Invalid chromatic effect '" + key + "': unknown font '" + font +
                    "' (valid: " + ChromaticAPI.getBaseFontNames() + ")");
        }
        effects.add(new Fx(k.name, k.named, movement, colorSpeed, moveSpeed, List.copyOf(cols), font));
    }

    @Info("""
            Shorthand for a static (non-scrolling) color gradient: `event.gradient('[sunrise]', ['#ff9a3c', '#ff3c7a'])`.
            """)
    public void gradient(String key, Object colors) {
        Key k = parseKey(key);
        List<Integer> cols = parseColors(colors);
        if (InlineEffects.define("none", 0f, null, cols) == null) {
            throw new IllegalArgumentException("Invalid chromatic gradient '" + key + "': need 1-16 colors");
        }
        effects.add(new Fx(k.name, k.named, "none", 0f, null, List.copyOf(cols), null));
    }

    @Info("Defines a plain color code: `event.color('[gt_blue]', '#2a6bff')`.")
    public void color(String key, Object rgb) {
        Key k = parseKey(key);
        colors.add(new ColorDef(k.name, k.named, parseColor(rgb)));
    }

    @Info("""
            Makes a code an outline modifier, like the config's [formatting] outline codes:
            `&[name]` outlines with the outline color, `&[name:RRGGBB]` with a specific one.
            """)
    public void outline(String key) {
        Key k = parseKey(key);
        if (k.named && k.name.contains(":")) throw new IllegalArgumentException("Outline names can't contain ':'");
        outlines.add(new OutlineDef(k.name, k.named));
    }

    @Info("Sets the default outline color used when an outline code doesn't specify one.")
    public void outlineColor(Object rgb) {
        outlineColor = parseColor(rgb);
    }

    public Definitions build() {
        return new Definitions(List.copyOf(effects), List.copyOf(colors), List.copyOf(outlines), outlineColor);
    }

    // ---- parsing / validation ----

    private record Key(String name, boolean named) {}

    private static Key parseKey(String key) {
        if (key == null || key.isEmpty()) throw new IllegalArgumentException("Chromatic code key can't be empty");

        if (key.length() > 1) {
            if (!(key.startsWith("[") && key.endsWith("]")) || key.length() < 3) {
                throw new IllegalArgumentException("Invalid chromatic code key '" + key +
                        "': use a single character ('q') or a bracketed name ('[ember]')");
            }
            String name = key.substring(1, key.length() - 1);
            if (name.contains("]") || name.contains("[") || name.contains("§") || name.contains(":") ||
                    name.startsWith("~")) {
                throw new IllegalArgumentException("Invalid chromatic name '" + key +
                        "': no brackets, '§', ':' and it can't start with '~' (that's inline syntax)");
            }
            return new Key(name, true);
        }

        char c = Character.toLowerCase(key.charAt(0));
        if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'k' && c <= 'o') || c == 'r' || c == 'x' ||
                c == '&' || c == '§' || c == '[' || c == ']') {
            throw new IllegalArgumentException("Chromatic code '" + key + "' collides with vanilla/formatting " +
                    "syntax (0-9, a-f, k-o, r, x, &, §, [ ]). Pick another character or use a [name].");
        }
        return new Key(String.valueOf(key.charAt(0)), false);
    }

    private static List<Integer> parseColors(@Nullable Object o) {
        if (o == null) throw new IllegalArgumentException("Missing 'colors'");
        if (o instanceof CharSequence cs) {
            String s = cs.toString().trim();
            if (s.equalsIgnoreCase("rainbow")) return RAINBOW;
            return Collections.singletonList(parseColor(s));
        }
        if (o instanceof Number) return Collections.singletonList(parseColor(o));
        if (o instanceof List<?> list) {
            List<Integer> out = new ArrayList<>(list.size());
            for (Object item : list) out.add(parseColor(item));
            return out;
        }
        throw new IllegalArgumentException("'colors' must be an array of colors or 'rainbow', got " + o);
    }

    private static int parseColor(@Nullable Object o) {
        if (o instanceof Number n) return n.intValue() & 0xFFFFFF;
        if (o instanceof CharSequence cs) {
            String h = cs.toString().trim();
            if (h.startsWith("#")) h = h.substring(1);
            else if (h.startsWith("0x") || h.startsWith("0X")) h = h.substring(2);
            if (h.length() == 3) {
                h = "" + h.charAt(0) + h.charAt(0) + h.charAt(1) + h.charAt(1) + h.charAt(2) + h.charAt(2);
            }
            if (h.length() == 6) {
                try {
                    return Integer.parseInt(h, 16);
                } catch (NumberFormatException ignored) {}
            }
        }
        throw new IllegalArgumentException("Invalid color '" + o + "': use '#RRGGBB', 'RRGGBB', '#RGB' or a number");
    }

    private static float number(Object o, String what) {
        if (o instanceof Number n) return n.floatValue();
        throw new IllegalArgumentException("'" + what + "' must be a number, got " + o);
    }

    private static List<String> sorted() {
        return ChromaticJS.movements();
    }
}
