package net.phoenix.chromatic_codes.mixin;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.util.StringDecomposer;
import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.ChromaticColors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlainTextContents.LiteralContents.class)
public abstract class MixinLiteralContents {

    // Force the compiler to skip mapping verification via remap = false
    // and provide the exact bytecode method descriptor path for safety.
    @Inject(
            method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private <T> void phoenix$decomposeLiteral(FormattedText.StyledContentConsumer<T> consumer, Style style,
                                              CallbackInfoReturnable<Optional<T>> cir) {
        // The decompile verifies the record property getter method name is exactly .text()
        String rawText = ((PlainTextContents.LiteralContents) (Object) this).text();

        if (!phoenix$containsActionableCode(rawText)) return;

        String processed = phoenix$replaceCustomAmpersands(rawText);

        @SuppressWarnings("unchecked")
        Optional<T>[] stopResult = new Optional[] { null };

        StringDecomposer.iterateFormatted(processed, style, (pos, currentStyle, codePoint) -> {
            String charStr = new String(Character.toChars(codePoint));
            Optional<T> result = consumer.accept(currentStyle, charStr);
            if (result.isPresent()) {
                stopResult[0] = result;
                return false;
            }
            return true;
        });

        cir.setReturnValue(stopResult[0] != null ? stopResult[0] : Optional.empty());
    }

    @Unique
    private static boolean phoenix$containsActionableCode(String s) {
        for (int i = 0; i < s.length() - 1; i++) {
            char c = s.charAt(i);
            char next = Character.toLowerCase(s.charAt(i + 1));

            boolean isCustomCode = ChromaticAPI.isRegistered(next) ||
                    ChromaticColors.CUSTOM_FORMATTING.containsKey(next) ||
                    ChromaticAPI.isOutlineCode(next) ||
                    next == '[';

            if ((c == '§' || c == '&') && isCustomCode) {
                return true;
            }

            if (c == '&' && net.minecraft.ChatFormatting.getByCode(next) != null) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static String phoenix$replaceCustomAmpersands(String s) {
        if (!s.contains("&")) return s;

        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                char lower = Character.toLowerCase(next);

                if (lower == '[') {
                    int closeIdx = s.indexOf(']', i + 2);
                    if (closeIdx != -1) {
                        String name = s.substring(i + 2, closeIdx);
                        if (ChromaticAPI.isNamedRegistered(name) ||
                                ChromaticAPI.isNamedOutlineCode(name) ||
                                ChromaticColors.NAMED_CUSTOM_FORMATTING
                                        .containsKey(ChromaticAPI.normalizeNamedKey(name))) {
                            sb.append('§').append(s, i + 1, closeIdx + 1);
                            i = closeIdx;
                            continue;
                        }
                    }
                } else if (ChromaticAPI.isRegistered(lower) ||
                        ChromaticColors.CUSTOM_FORMATTING.containsKey(lower) ||
                        ChromaticAPI.isOutlineCode(lower) ||
                        net.minecraft.ChatFormatting.getByCode(lower) != null) {

                            sb.append('§').append(next);
                            i++;
                            continue;
                        }
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
