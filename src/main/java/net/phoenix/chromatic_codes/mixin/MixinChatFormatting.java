package net.phoenix.chromatic_codes.mixin;

import net.minecraft.ChatFormatting;
import net.phoenix.ChromaticAPI;

import net.phoenix.chromatic_codes.api.ChromaticColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatFormatting.class)
public class MixinChatFormatting {
    @Inject(method = "getByCode", at = @At("HEAD"), cancellable = true)
    private static void phoenix$bypassVanillaCheck(char code, CallbackInfoReturnable<ChatFormatting> cir) {
        // If our API knows this character, tell Minecraft it's a "valid" code
        // We return WHITE as a placeholder; the Decomposer Mixin will override the style anyway.
        if (ChromaticAPI.isRegistered(code) || ChromaticColors.CUSTOM_FORMATTING.containsKey(Character.toLowerCase(code))) {
            cir.setReturnValue(ChatFormatting.WHITE);
        }
    }
}