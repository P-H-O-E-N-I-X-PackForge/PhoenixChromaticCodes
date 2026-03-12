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

    @Shadow float x;
    @Shadow float y;

    // We store the current offset to reset it accurately
    @Unique
    private float phoenix$currentOffsetX;
    @Unique
    private float phoenix$currentOffsetY;

    @ModifyVariable(method = "accept", at = @At("HEAD"), argsOnly = true)
    private Style phoenix$applyApiEffects(Style style) {
        if (style.getFont().getNamespace().equals("phoenix_chromatic_codes")) {
            IChromaticEffect effect = ChromaticAPI.getByFont(style.getFont());
            if (effect != null) {
                return style.withColor(effect.getRenderColor(0, this.x, this.y));
            }
        }
        return style;
    }

    @Inject(method = "accept", at = @At("HEAD"))
    private void phoenix$applyOffsets(int index, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        ResourceLocation font = style.getFont();
        if (font.getNamespace().equals("phoenix_chromatic_codes")) {
            IChromaticEffect effect = ChromaticAPI.getByFont(font);

            if (effect != null) {
                this.phoenix$currentOffsetX = effect.getXOffset(this.x, this.y);
                this.phoenix$currentOffsetY = effect.getYOffset(this.x, this.y);
                this.x += this.phoenix$currentOffsetX;
                this.y += this.phoenix$currentOffsetY;
            } else {
                // DEBUG: This will tell you if the font is correct but the API is missing the effect
                System.out.println("Movement Debug: Style has custom font " + font + " but no Effect is registered!");
                this.phoenix$currentOffsetX = 0;
                this.phoenix$currentOffsetY = 0;
            }
        }
    }

    @Inject(method = "accept", at = @At("RETURN"))
    private void phoenix$resetOffsets(int index, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        // Resetting at the end of the method ensures the next character starts at the correct 'base' position
        if (this.phoenix$currentOffsetX != 0 || this.phoenix$currentOffsetY != 0) {
            this.x -= this.phoenix$currentOffsetX;
            this.y -= this.phoenix$currentOffsetY;

            // Clean up
            this.phoenix$currentOffsetX = 0;
            this.phoenix$currentOffsetY = 0;
        }
    }
}