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

    // Force the compiler to skip mapping verification via remap = false
    // and provide the explicit descriptor signature for fromLegacyFormat(ChatFormatting)
    @Inject(
            method = "fromLegacyFormat(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/TextColor;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private static void phoenix$applyCustomHex(ChatFormatting formatting, CallbackInfoReturnable<TextColor> cir) {
        char lastCode = ChromaticColors.LAST_CODE.get();
        Integer customColor = ChromaticColors.CUSTOM_FORMATTING.get(lastCode);

        ChromaticColors.LAST_CODE.set(' '); // Always reset before any return path

        if (customColor != null) {
            cir.setReturnValue(TextColor.fromRgb(customColor));
        }
    }
}
