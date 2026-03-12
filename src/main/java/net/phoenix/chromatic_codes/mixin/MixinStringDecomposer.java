package net.phoenix.chromatic_codes.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import net.phoenix.ChromaticAPI;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringDecomposer.class)
public class MixinStringDecomposer {

    @Inject(method = "iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private static void phoenix$injectCustomStyles(String text, int skip, Style currentStyle, Style defaultStyle,
                                                   FormattedCharSink sink, CallbackInfoReturnable<Boolean> cir) {
        if (text.indexOf('\u00a7') == -1) return;

        int len = text.length();
        Style style = currentStyle;

        for (int j = skip; j < len; ++j) {
            char c0 = text.charAt(j);

            if (c0 == '\u00a7' && j + 1 < len) {
                char c1 = Character.toLowerCase(text.charAt(j + 1));

                // Check if this character is registered in our Dynamic API
                ResourceLocation effectFont = ChromaticAPI.getFontForCode(c1);

                if (effectFont != null) {
                    style = style.withFont(effectFont);
                    j++;
                    continue;
                }

                // Fallback to vanilla
                ChatFormatting cf = ChatFormatting.getByCode(c1);
                if (cf != null) {
                    style = (cf == ChatFormatting.RESET) ? defaultStyle : style.applyLegacyFormat(cf);
                }
                j++;
            } else {
                if (!sink.accept(j, style, c0)) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
        cir.setReturnValue(true);
    }
}
