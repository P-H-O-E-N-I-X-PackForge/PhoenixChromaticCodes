package net.phoenix.chromatic_codes.mixin;

import dev.ftb.mods.ftblibrary.util.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

@Mixin(value = StringUtils.class, remap = false)
public class MixinFTBStringUtils {

    @Shadow
    @Final
    @Mutable
    private static Pattern FORMATTING_CODE_PATTERN;

    @Inject(method = "<clinit>", at = @At("RETURN"), remap = false)
    private static void phoenix$expandFormattingPattern(CallbackInfo ci) {
        FORMATTING_CODE_PATTERN = Pattern.compile("(?i)[\\&\\u00a7](.)");
    }
}
