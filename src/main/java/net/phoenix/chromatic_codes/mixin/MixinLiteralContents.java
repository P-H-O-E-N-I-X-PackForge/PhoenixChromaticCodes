package net.phoenix.chromatic_codes.mixin;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.util.StringDecomposer;
import net.phoenix.ChromaticAPI;
import net.phoenix.chromatic_codes.api.ChromaticColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LiteralContents.class)
public abstract class MixinLiteralContents {

    @Inject(
            method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true
    )
    private <T> void phoenix$decomposeLiteral(FormattedText.StyledContentConsumer<T> consumer, Style style,
                                              CallbackInfoReturnable<Optional<T>> cir) {

        String rawText = ((LiteralContents) (Object) this).text();
        if (rawText.indexOf('&') == -1) return;

        StringBuilder sb = new StringBuilder();
        boolean changed = false;

        for (int i = 0; i < rawText.length(); i++) {
            char c = rawText.charAt(i);

            if (c == '&' && i + 1 < rawText.length()) {
                char next = Character.toLowerCase(rawText.charAt(i + 1));

                if (phoenixChromaticCodes$isFormatCode(next)) {
                    sb.append('§').append(next);
                    changed = true;
                    i++; // 🔥 critical fix
                    continue;
                }
            }

            sb.append(c);
        }

        if (!changed) return;

        final Optional<T>[] resultHolder = (Optional<T>[]) new Optional[1];
        resultHolder[0] = Optional.empty();

        StringDecomposer.iterateFormatted(sb.toString(), style, (index, currentStyle, codePoint) -> {
            Optional<T> result = consumer.accept(currentStyle, new String(Character.toChars(codePoint)));
            if (result.isPresent()) {
                resultHolder[0] = result;
                return false;
            }
            return true;
        });

        cir.setReturnValue(resultHolder[0]);
    }

    @Unique
    private boolean phoenixChromaticCodes$isFormatCode(char c) {
        return (c >= '0' && c <= '9') ||
                (c >= 'a' && c <= 'f') ||
                (c >= 'k' && c <= 'o') ||
                c == 'r' || c == 'x' ||
                ChromaticAPI.isRegistered(c) ||
                ChromaticColors.CUSTOM_FORMATTING.containsKey(c);
    }
}