package net.phoenix.chromatic_codes.mixin;

import net.minecraft.ChatFormatting;
import net.phoenix.ChromaticAPI;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatFormatting.class)
public class MixinChatFormatting {

    @Inject(method = "getByCode", at = @At("HEAD"), cancellable = true)
    private static void phoenix$handleCustomCodes(char code, CallbackInfoReturnable<ChatFormatting> cir) {
        char lower = Character.toLowerCase(code);
        // If our API knows this character, treat it as a valid code (return white placeholder)
        if (ChromaticAPI.isRegistered(lower)) {
            cir.setReturnValue(ChatFormatting.WHITE);
        }
    }
}
