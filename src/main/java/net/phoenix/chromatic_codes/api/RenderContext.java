package net.phoenix.chromatic_codes.api;

public class RenderContext {

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    public static boolean enter() {
        int d = DEPTH.get();
        DEPTH.set(d + 1);
        return d == 0; // only true on first entry
    }

    public static void exit() {
        int d = DEPTH.get() - 1;
        DEPTH.set(Math.max(d, 0));
    }

    public static boolean isRendering() {
        return DEPTH.get() > 0;
    }
}