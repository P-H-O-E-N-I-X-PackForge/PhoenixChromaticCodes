package net.phoenix.chromatic_codes.mixin;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.util.StringDecomposer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LiteralContents.class)
public abstract class MixinLiteralContents {

    /**
     * This intercepts the styled visit method.
     * Instead of sending the raw text, we decompose it so § codes are processed.
     */
    @Inject(method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private <T> void phoenix$decomposeLiteral(FormattedText.StyledContentConsumer<T> consumer, Style style, CallbackInfoReturnable<Optional<T>> cir) {
        // Access the text from the record.
        // Since LiteralContents is a record, we use the accessor method 'text()'
        String rawText = ((LiteralContents)(Object)this).text();

        // Convert & to § before processing
        String processed = rawText.replace('&', '\u00a7');

        // This call triggers your MixinStringDecomposer!
        // It breaks the string into pieces, each with the correct Font/Color.
        boolean finished = StringDecomposer.iterateFormatted(processed, style, (pos, currentStyle, codePoint) -> {
            String charStr = new String(Character.toChars(codePoint));
            return consumer.accept(currentStyle, charStr).isEmpty();
        });

        cir.setReturnValue(finished ? Optional.empty() : Optional.of((T) Boolean.FALSE));
    }
}