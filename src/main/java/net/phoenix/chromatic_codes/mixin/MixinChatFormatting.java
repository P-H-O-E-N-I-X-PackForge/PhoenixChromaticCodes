package net.phoenix.chromatic_codes.mixin;

import net.minecraft.ChatFormatting;
import net.phoenix.chromatic_codes.api.ChromaticColors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatFormatting.class)
public class MixinChatFormatting {

    @Inject(method = "getByCode", at = @At("HEAD"), cancellable = true)
    private static void phoenix$handleCustomCodes(char code, CallbackInfoReturnable<ChatFormatting> cir) {
        char lower = Character.toLowerCase(code);
        if (ChromaticColors.CUSTOM_FORMATTING.containsKey(lower)) {
            ChromaticColors.LAST_CODE.set(lower);
            cir.setReturnValue(ChatFormatting.LIGHT_PURPLE);
        } else {
            ChromaticColors.LAST_CODE.set(' ');
        }
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(stringValue = "(?i)§[0-9A-FK-OR]"))
    private static String phoenix$extendRegex(String original) {
        return "(?i)§[0-9A-Z\\x21-\\x7E]";
    }
}
