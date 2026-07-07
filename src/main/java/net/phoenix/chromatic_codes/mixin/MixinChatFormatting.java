package net.phoenix.chromatic_codes.mixin;

import net.minecraft.ChatFormatting;
import net.phoenix.chromatic_codes.ChromaticAPI;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatFormatting.class)
public class MixinChatFormatting {

    // Force the compiler to skip mapping verification via remap = false
    // and provide the explicit descriptor signature for getByCode(char)
    @Inject(
            method = "getByCode(C)Lnet/minecraft/ChatFormatting;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private static void phoenix$handleCustomCodes(char code, CallbackInfoReturnable<ChatFormatting> cir) {
        char lower = Character.toLowerCase(code);
        // If our API knows this character, treat it as a valid code (return white placeholder)
        if (ChromaticAPI.isRegistered(lower)) {
            cir.setReturnValue(ChatFormatting.WHITE);
        }
    }
}
