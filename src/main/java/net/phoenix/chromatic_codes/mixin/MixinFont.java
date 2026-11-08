package net.phoenix.chromatic_codes.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.chromatic_codes.ChromaticAPI;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Inline-defined effects (see {@link InlineEffects}) live in font ids that don't exist as resources. Without this,
 * {@code FontManager} would hand back its missing-glyph font set for them. The Style keeps the inline id (that's where
 * the effect is read from); only the font <i>set</i> lookup is redirected to the default font.
 */
@Mixin(Font.class)
public abstract class MixinFont {

    // Force the compiler to skip mapping verification via remap = false
    @ModifyVariable(method = "getFontSet", at = @At("HEAD"), argsOnly = true, remap = false)
    private ResourceLocation phoenix$resolveInlineFont(ResourceLocation fontId) {
        return ChromaticAPI.renderFont(fontId);
    }
}
