package net.phoenix.chromatic_codes.mixin;

import net.minecraft.resources.ResourceLocation;
import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.IChromaticEffect;
import net.phoenix.chromatic_codes.api.IChromaticLayoutExtensions;

import icyllis.modernui.graphics.text.FontCollection;
import icyllis.modernui.mc.text.TextLayout;
import icyllis.modernui.mc.text.TextLayoutEngine;
import icyllis.modernui.mc.text.TextLayoutProcessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = TextLayoutProcessor.class, remap = false)
public abstract class MixinTextLayoutProcessor {

    @Shadow
    private IntArrayList mGlyphFlags;

    @Unique
    private final List<IChromaticEffect> phoenix$glyphEffects = new ArrayList<>();

    @Unique
    private int phoenix$sizeBeforeRun;

    @Inject(method = "reset", at = @At("TAIL"))
    private void phoenix$onReset(CallbackInfo ci) {
        phoenix$glyphEffects.clear();
    }

    // Inline-defined effects (see InlineEffects) live in font ids that don't exist as resources.
    // Without this, ModernUI's own font lookup inside handleStyleRun would resolve them to its
    // missing-glyph fallback and render boxes. fontName itself is left untouched (and still carries
    // the inline definition for the ChromaticAPI.getByFont lookup below) - only this lookup is redirected.
    @Redirect(method = "handleStyleRun",
              remap = false,
              at = @At(value = "INVOKE",
                       remap = false,
                       target = "Licyllis/modernui/mc/text/TextLayoutEngine;getFontCollection(Lnet/minecraft/resources/ResourceLocation;)Licyllis/modernui/graphics/text/FontCollection;"))
    private FontCollection phoenix$resolveInlineFont(TextLayoutEngine engine, ResourceLocation fontName) {
        return engine.getFontCollection(ChromaticAPI.renderFont(fontName));
    }

    // Record how many glyphs existed before this run's shaping call, so we can add exactly
    // as many effect entries as glyphs actually appeared - counting per mGlyphFlags.add()
    // call site relies on the shaper producing exactly one glyph per loop iteration, which
    // isn't guaranteed (kerning/complex shaping can add/merge glyphs); measuring the real
    // growth in mGlyphFlags avoids that class of alignment bug entirely.
    @Inject(method = "handleStyleRun", at = @At("HEAD"))
    private void phoenix$onStyleRunStart(char[] text, int start, int limit, boolean isRtl,
                                         int styleFlags, ResourceLocation fontName, CallbackInfo ci) {
        phoenix$sizeBeforeRun = mGlyphFlags.size();
    }

    @Inject(method = "handleStyleRun", at = @At("RETURN"))
    private void phoenix$onStyleRunEnd(char[] text, int start, int limit, boolean isRtl,
                                       int styleFlags, ResourceLocation fontName, CallbackInfo ci) {
        int added = mGlyphFlags.size() - phoenix$sizeBeforeRun;
        if (added <= 0) return;
        IChromaticEffect effect = ChromaticAPI.getByFont(fontName);

        // TextLayout.drawText never emits a vertex/color call for whitespace glyphs (nothing
        // to draw), so the draw-side glyph counter never advances for them either. If we added
        // an entry per character here, every space would silently absorb one array slot that
        // drawing will never consume, shifting every effect glyph after it by one. Skip
        // whitespace here too so the two counters stay in lockstep - but only when we can map
        // 1:1 (added == char count); if shaping merged/expanded glyphs, fall back to filling
        // blindly rather than guessing which glyphs the merge came from.
        if (added == limit - start) {
            for (int i = start; i < limit; i++) {
                if (!Character.isWhitespace(text[i])) {
                    phoenix$glyphEffects.add(effect);
                }
            }
        } else {
            for (int i = 0; i < added; i++) {
                phoenix$glyphEffects.add(effect);
            }
        }
    }

    @Inject(method = "createNewLayout", at = @At("RETURN"))
    private void phoenix$attachEffects(int resLevel, int computeFlags,
                                       CallbackInfoReturnable<TextLayout> cir) {
        Object layout = cir.getReturnValue();
        if (layout instanceof IChromaticLayoutExtensions ext && !phoenix$glyphEffects.isEmpty()) {
            ext.phoenix$setGlyphEffects(phoenix$glyphEffects.toArray(new IChromaticEffect[0]));
        }
    }
}
