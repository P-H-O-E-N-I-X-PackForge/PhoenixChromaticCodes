package net.phoenix.chromatic_codes.mixin;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.util.StringDecomposer;
import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.ChromaticColors;
import net.phoenix.chromatic_codes.compat.GuideMeCompat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LiteralContents.class)
public abstract class MixinLiteralContents {

    @Inject(method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true)
    private <T> void phoenix$decomposeLiteral(FormattedText.StyledContentConsumer<T> consumer, Style style,
                                              CallbackInfoReturnable<Optional<T>> cir) {
        if (GuideMeCompat.shouldSkip()) return;

        String rawText = ((LiteralContents) (Object) this).text();

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

    // A bracket is only "actionable" if the name inside it is actually registered. Flagging every
    // "&[" / "§[" regardless (as this used to) meant plain text with an unregistered bracket name
    // in it - e.g. a lang entry someone wrote ahead of defining the code - still got routed through
    // our replacement visit() below instead of vanilla's own, which iterates the text differently
    // (codepoint-by-codepoint via StringDecomposer.iterateFormatted vs. vanilla's bulk visit) and
    // throws off tooltip/line-wrapping callers that truncate based on how visit() is called.
    @Unique
    private static boolean phoenix$containsActionableCode(String s) {
        for (int i = 0; i < s.length() - 1; i++) {
            char c = s.charAt(i);
            if (c != '§' && c != '&') continue;

            char next = s.charAt(i + 1);
            char lower = Character.toLowerCase(next);

            if (lower == '[') {
                int closeIdx = s.indexOf(']', i + 2);
                if (closeIdx != -1) {
                    String name = s.substring(i + 2, closeIdx);
                    if (ChromaticAPI.isNamedRegistered(name) ||
                            ChromaticAPI.isNamedOutlineCode(name) ||
                            ChromaticColors.NAMED_CUSTOM_FORMATTING
                                    .containsKey(ChromaticAPI.normalizeNamedKey(name)) ||
                            ChromaticAPI.isInlineCode(name)) {
                        return true;
                    }
                }
            } else if (ChromaticAPI.isRegistered(lower) ||
                    ChromaticColors.CUSTOM_FORMATTING.containsKey(lower) ||
                    ChromaticAPI.isOutlineCode(lower) ||
                    (c == '&' && net.minecraft.ChatFormatting.getByCode(lower) != null)) {
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
                                        .containsKey(ChromaticAPI.normalizeNamedKey(name)) ||
                                ChromaticAPI.isInlineCode(name)) {
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
