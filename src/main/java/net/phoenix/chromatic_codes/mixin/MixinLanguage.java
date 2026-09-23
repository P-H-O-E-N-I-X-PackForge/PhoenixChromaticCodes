package net.phoenix.chromatic_codes.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.ChromaticColors;
import net.phoenix.chromatic_codes.compat.GuideMeCompat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Language.class)
public abstract class MixinLanguage {

    @Inject(method = "getOrDefault(Ljava/lang/String;)Ljava/lang/String;", at = @At("RETURN"), cancellable = true)
    private void phoenix$convertAmpersands(String key, CallbackInfoReturnable<String> cir) {
        if (GuideMeCompat.shouldSkip()) return;

        String value = cir.getReturnValue();
        if (value == null || value.indexOf('&') == -1) return;

        String converted = phoenix$replaceCustomAmpersands(value);
        if (!converted.equals(value)) {
            cir.setReturnValue(converted);
        }
    }

    @Unique
    private static String phoenix$replaceCustomAmpersands(String s) {
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
                        ChatFormatting.getByCode(lower) != null) {

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
