package net.phoenix.chromatic_codes.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.phoenix.chromatic_codes.api.ChromaticColors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextColor.class)
public class MixinTextColor {

    /**
     * @author Phoenix
     * @reason Redirect custom formatting codes to their registered hex colors
     */
    @Inject(method = "fromLegacyFormat", at = @At("HEAD"), cancellable = true)
    private static void phoenix$applyCustomHex(ChatFormatting formatting, CallbackInfoReturnable<TextColor> cir) {

        char lastCode = ChromaticColors.LAST_CODE.get();

        if (lastCode != ' ' && ChromaticColors.CUSTOM_FORMATTING.containsKey(lastCode)) {
            int hex = ChromaticColors.CUSTOM_FORMATTING.get(lastCode);
            cir.setReturnValue(TextColor.fromRgb(hex));
        }
    }
}
