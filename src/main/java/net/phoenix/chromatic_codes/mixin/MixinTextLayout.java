package net.phoenix.chromatic_codes.mixin;

import net.phoenix.chromatic_codes.api.IChromaticEffect;
import net.phoenix.chromatic_codes.api.IChromaticLayoutExtensions;

import icyllis.modernui.mc.text.TextLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = TextLayout.class, remap = false)
public abstract class MixinTextLayout implements IChromaticLayoutExtensions {

    @Unique
    private IChromaticEffect[] phoenix$glyphEffects;

    @Override
    public IChromaticEffect[] phoenix$getGlyphEffects() {
        return phoenix$glyphEffects;
    }

    @Override
    public void phoenix$setGlyphEffects(IChromaticEffect[] effects) {
        this.phoenix$glyphEffects = effects;
    }
}
