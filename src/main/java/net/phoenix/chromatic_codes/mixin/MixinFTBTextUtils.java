package net.phoenix.chromatic_codes.mixin;

import net.minecraft.network.chat.Component;
import net.phoenix.ChromaticAPI;
import net.phoenix.chromatic_codes.api.ChromaticColors;

import dev.ftb.mods.ftblibrary.util.client.ClientTextComponentUtils;
import dev.ftb.mods.ftbquests.util.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TextUtils.class, remap = false)
public class MixinFTBTextUtils {

    @Redirect(
              method = "parseRawText",
              remap = false,
              at = @At(
                       value = "INVOKE",
                       remap = false,
                       target = "Ldev/ftb/mods/ftblibrary/util/client/ClientTextComponentUtils;parse(Ljava/lang/String;)Lnet/minecraft/network/chat/Component;"))
    private static Component phoenix$modifyParse(String str) {
        String replaced = phoenixChromaticCodes$replaceAmpersands(str);

        if (phoenixChromaticCodes$containsActionableCode(replaced)) {
            return Component.literal(replaced);
        }

        return ClientTextComponentUtils.parse(replaced);
    }

    @Unique
    private static boolean phoenixChromaticCodes$containsActionableCode(String s) {
        for (int i = 0; i < s.length() - 1; i++) {
            if (s.charAt(i) == '§') {
                char next = Character.toLowerCase(s.charAt(i + 1));
                if (ChromaticAPI.isRegistered(next) || ChromaticColors.CUSTOM_FORMATTING.containsKey(next)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Unique
    private static String phoenixChromaticCodes$replaceAmpersands(String str) {
        if (str == null || str.isEmpty() || !str.contains("&")) return str;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '&' && i + 1 < str.length()) {
                char next = str.charAt(i + 1);
                if (phoenixChromaticCodes$isAnyValidCode(next)) {
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
    private static boolean phoenixChromaticCodes$isAnyValidCode(char c) {
        char lower = Character.toLowerCase(c);
        return (lower >= '0' && lower <= '9') ||
                (lower >= 'a' && lower <= 'f') ||
                (lower >= 'k' && lower <= 'o') ||
                lower == 'r' ||
                ChromaticAPI.isRegistered(lower) ||
                ChromaticColors.CUSTOM_FORMATTING.containsKey(lower);
    }
}
