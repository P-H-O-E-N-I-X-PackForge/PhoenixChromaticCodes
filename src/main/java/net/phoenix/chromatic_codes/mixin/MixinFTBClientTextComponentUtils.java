package net.phoenix.chromatic_codes.mixin;

import net.minecraft.network.chat.Component;
import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.api.ChromaticColors;

import dev.ftb.mods.ftblibrary.util.client.ClientTextComponentUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientTextComponentUtils.class, remap = false)
public class MixinFTBClientTextComponentUtils {

    @Unique
    private static final Logger CHROMATIC_LOGGER = LogManager.getLogger("ChromaticCodes/FTB");

    @Unique
    private static final String FTB_RESERVED = "#$";

    @Inject(method = "parse", at = @At("HEAD"), cancellable = true, remap = false)
    private static void phoenix$interceptParse(String str, CallbackInfoReturnable<Component> cir) {
        if (str == null || str.isEmpty()) return;

        String replaced = phoenix$replaceAmpersands(str);

        if (phoenix$containsActionableCode(replaced)) {

            cir.setReturnValue(Component.literal(replaced.replace("\\n", "\n")));
        }
    }

    @Unique
    private static boolean phoenix$containsActionableCode(String s) {
        for (int i = 0; i < s.length() - 1; i++) {
            if (s.charAt(i) == '§') {
                char next = Character.toLowerCase(s.charAt(i + 1));
                if (FTB_RESERVED.indexOf(next) != -1) continue;
                if (ChromaticAPI.isRegistered(next) ||
                        ChromaticColors.CUSTOM_FORMATTING.containsKey(next) ||
                        ChromaticAPI.isOutlineCode(next) ||
                        next == '[') {
                    return true;
                }
            }
        }
        return false;
    }

    @Unique
    private static String phoenix$replaceAmpersands(String str) {
        if (str == null || str.isEmpty() || !str.contains("&")) return str;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '&' && i + 1 < str.length()) {
                char next = str.charAt(i + 1);
                char lower = Character.toLowerCase(next);

                if (lower == '[') {
                    int closeIdx = str.indexOf(']', i + 2);
                    if (closeIdx != -1) {
                        String name = str.substring(i + 2, closeIdx);
                        if (ChromaticAPI.isNamedRegistered(name) ||
                                ChromaticAPI.isNamedOutlineCode(name) ||
                                ChromaticColors.NAMED_CUSTOM_FORMATTING
                                        .containsKey(ChromaticAPI.normalizeNamedKey(name))) {
                            sb.append('§').append(str, i + 1, closeIdx + 1);
                            i = closeIdx;
                            continue;
                        }
                    }
                }

                if (FTB_RESERVED.indexOf(next) != -1) {
                    if (phoenix$isOurCode(lower)) {
                        CHROMATIC_LOGGER.warn(
                                "Chromatic Codes: '&{}' is reserved by FTB Quests inside quest text and cannot " +
                                        "be used as a formatting code here. Use it in chat, books, or signs instead.",
                                next);
                    }
                    sb.append(c).append(next);
                    i++;
                    continue;
                }

                if (phoenix$isAnyValidCode(next)) {
                    sb.append('§').append(next);
                    i++;
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    @Unique
    private static boolean phoenix$isOurCode(char lower) {
        return ChromaticAPI.isRegistered(lower) || ChromaticColors.CUSTOM_FORMATTING.containsKey(lower) ||
                ChromaticAPI.isOutlineCode(lower);
    }

    @Unique
    private static boolean phoenix$isAnyValidCode(char c) {
        char lower = Character.toLowerCase(c);
        return (lower >= '0' && lower <= '9') ||
                (lower >= 'a' && lower <= 'f') ||
                (lower >= 'k' && lower <= 'o') ||
                lower == 'r' ||
                ChromaticAPI.isRegistered(lower) ||
                ChromaticColors.CUSTOM_FORMATTING.containsKey(lower);
    }
}
