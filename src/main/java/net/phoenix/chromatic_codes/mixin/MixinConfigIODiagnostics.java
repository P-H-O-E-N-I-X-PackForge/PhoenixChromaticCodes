package net.phoenix.chromatic_codes.mixin;

import net.phoenix.chromatic_codes.PhoenixChromaticCodes;
import net.phoenix.chromatic_codes.config.ConfigFileSanitizer;
import net.phoenix.chromatic_codes.config.ModConfig;
import net.phoenix.chromatic_codes.diagnostics.ConfigDiagnostics;

import dev.toma.configuration.config.ConfigHolder;
import dev.toma.configuration.config.io.ConfigIO;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into the Configuration library's own read/write cycle, scoped to just our config id:
 * - logs the raw file and the in-memory arrays at each step (diagnostic logging, kept to help
 *   track down anything similar in the future)
 * - normalizes any tab-indented lines back to spaces before the real read happens, working around
 *   a YamlFormat bug that otherwise silently drops everything after a tab-indented line (see
 *   ConfigFileSanitizer)
 */
@Mixin(value = ConfigIO.class, remap = false)
public class MixinConfigIODiagnostics {

    @Inject(method = "readConfig", at = @At("HEAD"), remap = false)
    private static void phoenix$beforeRead(ConfigHolder<?> holder, CallbackInfo ci) {
        if (!phoenix$isOurs(holder)) return;
        ConfigDiagnostics.logRawFile(ConfigIO.getConfigFile(holder), "BEFORE readConfig");
        ConfigFileSanitizer.normalizeLeadingTabs(ConfigIO.getConfigFile(holder));
    }

    @Inject(method = "readConfig", at = @At("RETURN"), remap = false)
    private static void phoenix$afterRead(ConfigHolder<?> holder, CallbackInfo ci) {
        if (!phoenix$isOurs(holder)) return;
        ConfigDiagnostics.logInMemoryArrays((ModConfig) holder.getConfigInstance(), "AFTER readConfig");
    }

    @Inject(method = "writeConfig", at = @At("HEAD"), remap = false)
    private static void phoenix$beforeWrite(ConfigHolder<?> holder, CallbackInfo ci) {
        if (!phoenix$isOurs(holder)) return;
        ConfigDiagnostics.logInMemoryArrays((ModConfig) holder.getConfigInstance(), "BEFORE writeConfig");
    }

    @Inject(method = "writeConfig", at = @At("RETURN"), remap = false)
    private static void phoenix$afterWrite(ConfigHolder<?> holder, CallbackInfo ci) {
        if (!phoenix$isOurs(holder)) return;
        ConfigDiagnostics.logRawFile(ConfigIO.getConfigFile(holder), "AFTER writeConfig");
    }

    private static boolean phoenix$isOurs(ConfigHolder<?> holder) {
        return holder != null && PhoenixChromaticCodes.MOD_ID.equals(holder.getConfigId());
    }
}
