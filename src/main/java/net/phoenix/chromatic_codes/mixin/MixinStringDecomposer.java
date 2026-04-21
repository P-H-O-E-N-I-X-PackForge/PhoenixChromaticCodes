package net.phoenix.chromatic_codes.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import net.phoenix.ChromaticAPI;
import net.phoenix.chromatic_codes.api.ChromaticColors;
import net.phoenix.chromatic_codes.api.RenderContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringDecomposer.class)
public class MixinStringDecomposer {

    @Inject(
            method = "iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void phoenix$injectCustomStyles(String text, int skip, Style currentStyle, Style defaultStyle,
                                                   FormattedCharSink sink, CallbackInfoReturnable<Boolean> cir) {

        if (text.indexOf('§') == -1) return;

        int len = text.length();
        Style style = currentStyle;

        for (int j = skip; j < len; ++j) {
            char c0 = text.charAt(j);

            if (c0 == '§' && j + 1 < len) {
                char code = Character.toLowerCase(text.charAt(j + 1));

                // 🎨 Custom font effect
                ResourceLocation font = ChromaticAPI.getFontForCode(code);
                if (font != null) {
                    style = style.withFont(font).withColor((TextColor) null);
                    j++;
                    continue;
                }

                // 🌈 Custom color
                if (ChromaticColors.CUSTOM_FORMATTING.containsKey(code)) {
                    int hex = ChromaticColors.CUSTOM_FORMATTING.get(code);
                    style = style.withColor(TextColor.fromRgb(hex));
                    j++;
                    continue;
                }

                // 🧱 Vanilla fallback
                ChatFormatting cf = ChatFormatting.getByCode(code);
                if (cf != null) {
                    if (cf == ChatFormatting.RESET) {
                        style = defaultStyle.withFont(Style.DEFAULT_FONT);
                    } else {
                        style = style.applyLegacyFormat(cf);
                        if (cf.isColor()) {
                            style = style.withFont(Style.DEFAULT_FONT);
                        }
                    }
                }

                j++;
                continue;
            }

            if (!sink.accept(j, style, c0)) {
                cir.setReturnValue(false);
                return;
            }
        }

        cir.setReturnValue(true);
    }
}