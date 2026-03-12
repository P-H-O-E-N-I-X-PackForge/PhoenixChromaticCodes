package net.phoenix.chromatic_codes.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import net.phoenix.ChromaticAPI;
import net.phoenix.chromatic_codes.api.ChromaticColors;

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
        // Optimization: if no section symbol is present, let vanilla handle it
        if (text.indexOf('\u00a7') == -1) return;

        int len = text.length();
        Style style = currentStyle;

        for (int j = skip; j < len; ++j) {
            char c0 = text.charAt(j);

            // Detect formatting code start
            if (c0 == '\u00a7' && j + 1 < len) {
                char c1 = Character.toLowerCase(text.charAt(j + 1));

                // 1. Check for Dynamic Effects (Wave, Shake, etc.)
                ResourceLocation effectFont = ChromaticAPI.getFontForCode(c1);
                if (effectFont != null) {
                    // Update font and strip the color so the Mixin can inject the gradient
                    style = style.withFont(effectFont).withColor((TextColor) null);
                    j++;
                    continue;
                }

                // 2. Check for Custom Hex Colors (z, p, etc.)
                if (ChromaticColors.CUSTOM_FORMATTING.containsKey(c1)) {
                    // Set the thread-local so MixinTextColor knows which color to use
                    ChromaticColors.LAST_CODE.set(c1);

                    // Force the color update
                    int hex = ChromaticColors.CUSTOM_FORMATTING.get(c1);
                    style = style.withColor(TextColor.fromRgb(hex));
                    j++;
                    continue;
                }

                // 3. Fallback to Vanilla Formatting
                ChatFormatting cf = ChatFormatting.getByCode(c1);
                if (cf != null) {
                    if (cf == ChatFormatting.RESET) {
                        style = defaultStyle;
                        ChromaticColors.LAST_CODE.set(' '); // Clear custom code context
                    } else {
                        style = style.applyLegacyFormat(cf);
                    }
                }
                j++;
            } else {
                // Regular character: send to the sink with the current style
                if (!sink.accept(j, style, c0)) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
        cir.setReturnValue(true);
    }
}
