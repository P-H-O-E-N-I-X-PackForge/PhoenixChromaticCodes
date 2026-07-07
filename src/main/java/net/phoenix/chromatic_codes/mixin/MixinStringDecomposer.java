package net.phoenix.chromatic_codes.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.ChromaticColors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StringDecomposer.class, priority = 2000)
public class MixinStringDecomposer {

    // FIX: Bypassed mapping check via remap = false and cleaned descriptor syntax
    @Inject(
            method = "iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private static void phoenix$processModernUITextPipeline(String text, int skip, Style currentStyle,
                                                            Style defaultStyle,
                                                            FormattedCharSink sink,
                                                            CallbackInfoReturnable<Boolean> cir) {
        // Fast-fail if there are no formatting symbols whatsoever
        if (text.indexOf('\u00a7') == -1) return;

        ChromaticColors.LAST_CODE.set(' ');
        int len = text.length();
        Style style = currentStyle;

        for (int j = skip; j < len; ++j) {
            char c0 = text.charAt(j);

            if (c0 == '\u00a7' && j + 1 < len) {
                char c1 = Character.toLowerCase(text.charAt(j + 1));

                // 1. Support for Vanilla Hex Codes (§x§r§g§b...)
                if (c1 == 'x' && j + 13 < len) {
                    // FIX: Extract the optional result out of the modern DataResult container
                    TextColor hexColor = TextColor.parseColor(text.substring(j, j + 14))
                            .result()
                            .orElse(null);

                    if (hexColor != null) {
                        style = style.withColor(hexColor);
                        j += 13;
                        continue;
                    }
                }

                // 2. Named bracket codes: §[name]
                if (c1 == '[') {
                    int closeIdx = text.indexOf(']', j + 2);
                    if (closeIdx != -1) {
                        String name = text.substring(j + 2, closeIdx);

                        ResourceLocation namedFont = ChromaticAPI.getFontForNamedCode(name);
                        if (namedFont != null) {
                            style = style.withFont(namedFont).withColor((TextColor) null);
                            j = closeIdx;
                            continue;
                        }

                        String normalized = ChromaticAPI.normalizeNamedKey(name);
                        if (ChromaticColors.NAMED_CUSTOM_FORMATTING.containsKey(normalized)) {
                            int hex = ChromaticColors.NAMED_CUSTOM_FORMATTING.get(normalized);
                            style = style.withColor(TextColor.fromRgb(hex));
                            j = closeIdx;
                            continue;
                        }
                    }
                }

                // 3. Check for Dynamic Font Effects (Wave, Shake, etc.)
                ResourceLocation effectFont = ChromaticAPI.getFontForCode(c1);
                if (effectFont != null) {
                    style = style.withFont(effectFont).withColor((TextColor) null);
                    j++;
                    continue;
                }

                // 4. Check for Custom Hex Colors (z, p, etc.)
                if (ChromaticColors.CUSTOM_FORMATTING.containsKey(c1)) {
                    ChromaticColors.LAST_CODE.set(c1);
                    int hex = ChromaticColors.CUSTOM_FORMATTING.get(c1);
                    style = style.withColor(TextColor.fromRgb(hex));
                    j++;
                    continue;
                }

                // 5. Fallback cleanly to standard Vanilla Formatting
                ChatFormatting cf = ChatFormatting.getByCode(c1);
                if (cf != null) {
                    if (cf == ChatFormatting.RESET) {
                        style = defaultStyle;
                        ChromaticColors.LAST_CODE.set(' ');
                    } else if (cf.isColor()) {
                        style = style.withColor(TextColor.fromLegacyFormat(cf)).withFont(Style.DEFAULT_FONT);
                        ChromaticColors.LAST_CODE.set(' ');
                    } else {
                        style = style.applyLegacyFormat(cf);
                    }
                }
                j++;
            } else {
                // FIX: Restored Vanilla 1.21.1 High/Low Surrogate character processing rules
                if (Character.isHighSurrogate(c0)) {
                    if (j + 1 >= len) {
                        if (!sink.accept(j, style, 65533)) {
                            cir.setReturnValue(false);
                            return;
                        }
                        break;
                    }

                    char c2 = text.charAt(j + 1);
                    if (Character.isLowSurrogate(c2)) {
                        if (!sink.accept(j, style, Character.toCodePoint(c0, c2))) {
                            cir.setReturnValue(false);
                            return;
                        }
                        ++j;
                    } else if (!sink.accept(j, style, 65533)) {
                        cir.setReturnValue(false);
                        return;
                    }
                } else if (!phoenix$feedChar(style, sink, j, c0)) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
        cir.setReturnValue(true);
    }

    // Helper method matching internal 1.21.1 surrogate fallback rules
    @Unique
    private static boolean phoenix$feedChar(Style style, FormattedCharSink sink, int position, char character) {
        return Character.isSurrogate(character) ? sink.accept(position, style, 65533) :
                sink.accept(position, style, character);
    }
}
