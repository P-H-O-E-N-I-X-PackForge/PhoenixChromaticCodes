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

    @Inject(method = "fromLegacyFormat", at = @At("HEAD"), cancellable = true)
    private static void phoenix$applyCustomHex(ChatFormatting formatting, CallbackInfoReturnable<TextColor> cir) {
        // Since MixinChatFormatting returns WHITE for custom codes,
        // we check if the current 'lastCode' is registered in our API.
        char lastCode = ChromaticColors.LAST_CODE.get();
        Integer customColor = ChromaticColors.CUSTOM_FORMATTING.get(lastCode);

        if (customColor != null) {
            cir.setReturnValue(TextColor.fromRgb(customColor));
        }
    }
}
