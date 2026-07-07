package net.phoenix.chromatic_codes.mixin;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.IChromaticEffect;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// FIX: Target the interface type directly to guarantee successful method hook lookups
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

    @Unique
    private IChromaticEffect phoenix$previousEffect;

    /**
     * Applies the effect color to the style so vanilla has something to pass
     * to BakedGlyph.render as the red/green/blue arguments.
     */
    @ModifyVariable(
                    method = "accept(ILnet/minecraft/network/chat/Style;I)Z",
                    at = @At("HEAD"),
                    argsOnly = true,
                    remap = false)
    private Style phoenix$applyApiEffects(Style style) {
        if (style.getFont().getNamespace().equals("phoenix_chromatic_codes")) {
            IChromaticEffect effect = ChromaticAPI.getByFont(style.getFont());
            if (effect != null) {
                return style.withColor(effect.getRenderColor(0, this.x, this.y));
            }
        }
        return style;
    }

    /**
     * Sets the current effect on the thread-local and applies any X/Y positional offsets.
     */
    @Inject(
            method = "accept(ILnet/minecraft/network/chat/Style;I)Z",
            at = @At("HEAD"),
            remap = false)
    private void phoenix$applyOffsets(int index, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        ResourceLocation font = style.getFont();
        if (font.getNamespace().equals("phoenix_chromatic_codes")) {
            IChromaticEffect effect = ChromaticAPI.getByFont(font);
            if (effect != null) {
                if (effect != phoenix$previousEffect) {
                    ChromaticAPI.setSegmentStartX(this.x);
                    phoenix$previousEffect = effect;
                }
                ChromaticAPI.setCurrentEffect(effect);
                this.phoenix$currentOffsetX = effect.getXOffset(this.x, this.y);
                this.phoenix$currentOffsetY = effect.getYOffset(this.x, this.y);
                this.x += this.phoenix$currentOffsetX;
                this.y += this.phoenix$currentOffsetY;
                return;
            }
        }
        phoenix$previousEffect = null;
        ChromaticAPI.setCurrentEffect(null);
        this.phoenix$currentOffsetX = 0;
        this.phoenix$currentOffsetY = 0;
    }

    /**
     * Restores x/y after the glyph is drawn and clears the thread-local.
     */
    @Inject(
            method = "accept(ILnet/minecraft/network/chat/Style;I)Z",
            at = @At("RETURN"),
            remap = false)
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
