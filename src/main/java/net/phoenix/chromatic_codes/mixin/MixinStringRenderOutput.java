package net.phoenix.chromatic_codes.mixin;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.ChromaticAPI;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public abstract class MixinStringRenderOutput {

    @Shadow
    float x;
    @Shadow
    float y;

    @Unique
    private float phoenix$currentOffsetX;
    @Unique
    private float phoenix$currentOffsetY;

    /**
     * Applies the effect color to the style so vanilla has something to pass
     * to BakedGlyph.render as the red/green/blue arguments.
     * BakedGlyph then overrides those with per-vertex gradient colors.
     * For scale-only effects (breath, stretch) we still need a non-null color
     * here or vanilla won't render anything — white (0xFFFFFF) is fine.
     */
    @ModifyVariable(method = "accept", at = @At("HEAD"), argsOnly = true)
    private Style phoenix$applyApiEffects(Style style) {
        if (style.getFont().getNamespace().equals("phoenix_chromatic_codes")) {
            IChromaticEffect effect = ChromaticAPI.getByFont(style.getFont());
            if (effect != null) {
                // Use the effect's own color; for scale-only effects this is
                // just the base color (e.g. first color in the list).
                // BakedGlyph.render will override it with gradient vertices anyway.
                return style.withColor(effect.getRenderColor(0, this.x, this.y));
            }
        }
        return style;
    }

    /**
     * Sets the current effect on the thread-local (so MixinBakedGlyph can
     * pick it up) and applies any X/Y positional offsets (wave, shake, etc.).
     */
    @Inject(method = "accept", at = @At("HEAD"))
    private void phoenix$applyOffsets(int index, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        ResourceLocation font = style.getFont();
        if (font.getNamespace().equals("phoenix_chromatic_codes")) {
            IChromaticEffect effect = ChromaticAPI.getByFont(font);
            if (effect != null) {
                ChromaticAPI.setCurrentEffect(effect); // cleared at RETURN
                this.phoenix$currentOffsetX = effect.getXOffset(this.x, this.y);
                this.phoenix$currentOffsetY = effect.getYOffset(this.x, this.y);
                this.x += this.phoenix$currentOffsetX;
                this.y += this.phoenix$currentOffsetY;
                return;
            }
        }
        // Not a chromatic glyph — make sure no stale effect leaks
        ChromaticAPI.setCurrentEffect(null);
        this.phoenix$currentOffsetX = 0;
        this.phoenix$currentOffsetY = 0;
    }

    /**
     * Restores x/y after the glyph is drawn and clears the thread-local
     * so the shadow pass (and the next character) don't inherit it.
     */
    @Inject(method = "accept", at = @At("RETURN"))
    private void phoenix$resetOffsets(int index, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        if (this.phoenix$currentOffsetX != 0 || this.phoenix$currentOffsetY != 0) {
            this.x -= this.phoenix$currentOffsetX;
            this.y -= this.phoenix$currentOffsetY;
            this.phoenix$currentOffsetX = 0;
            this.phoenix$currentOffsetY = 0;
        }
        ChromaticAPI.setCurrentEffect(null);
    }
}
