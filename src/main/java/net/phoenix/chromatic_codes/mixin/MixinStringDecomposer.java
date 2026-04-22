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
        if (text.indexOf('\u00a7') == -1) return;

        // Only take over if there's actually a custom code in this string.
        // Pure-vanilla strings are left entirely to vanilla's own loop.
        boolean hasCustomCode = false;
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == '\u00a7') {
                char next = Character.toLowerCase(text.charAt(i + 1));
                if (ChromaticAPI.getFontForCode(next) != null ||
                        ChromaticColors.CUSTOM_FORMATTING.containsKey(next)) {
                    hasCustomCode = true;
                    break;
                }
            }
        }
        if (!hasCustomCode) return;

        // Reset LAST_CODE at the start of every decomposition so stale state
        // from a previous render never bleeds into unrelated text.
        ChromaticColors.LAST_CODE.set(' ');

        int len = text.length();
        Style style = currentStyle;

        for (int j = skip; j < len; ++j) {
            char c0 = text.charAt(j);

            if (c0 == '\u00a7' && j + 1 < len) {
                char c1 = Character.toLowerCase(text.charAt(j + 1));

                // 1. Support for Vanilla Hex Codes (§x§r§g§b...)
                if (c1 == 'x' && j + 13 < len) {
                    TextColor hexColor = TextColor.parseColor(text.substring(j, j + 14));
                    if (hexColor != null) {
                        style = style.withColor(hexColor);
                        j += 13;
                        continue;
                    }
                }

                // 2. Check for Dynamic Effects (Wave, Shake, etc.)
                ResourceLocation effectFont = ChromaticAPI.getFontForCode(c1);
                if (effectFont != null) {
                    style = style.withFont(effectFont).withColor((TextColor) null);
                    j++;
                    continue;
                }

                // 3. Check for Custom Hex Colors (z, p, etc.)
                if (ChromaticColors.CUSTOM_FORMATTING.containsKey(c1)) {
                    ChromaticColors.LAST_CODE.set(c1);
                    int hex = ChromaticColors.CUSTOM_FORMATTING.get(c1);
                    style = style.withColor(TextColor.fromRgb(hex));
                    j++;
                    continue;
                }

                // 4. Fallback to Vanilla Formatting
                ChatFormatting cf = ChatFormatting.getByCode(c1);
                if (cf != null) {
                    if (cf == ChatFormatting.RESET) {
                        style = defaultStyle;
                        ChromaticColors.LAST_CODE.set(' ');
                    } else if (cf.isColor()) {
                        style = style.withColor(TextColor.fromLegacyFormat(cf))
                                .withFont(Style.DEFAULT_FONT);
                        ChromaticColors.LAST_CODE.set(' ');
                    } else {
                        style = style.applyLegacyFormat(cf);
                    }
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
