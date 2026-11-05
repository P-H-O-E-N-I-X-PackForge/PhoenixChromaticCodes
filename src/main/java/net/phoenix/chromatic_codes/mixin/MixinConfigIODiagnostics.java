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
 * - normalizes any tab-indented lines back to spaces before the real read happens, working around
 * a YamlFormat bug that otherwise silently drops everything after a tab-indented line (see
 * ConfigFileSanitizer). Always on - this is a bug workaround, not diagnostics.
 * - logs the raw file and the in-memory arrays at each step, only when
 * compatibility.verboseConfigDiagnostics is enabled in the config.
 */
@Mixin(value = ConfigIO.class, remap = false)
public class MixinConfigIODiagnostics {

    @Inject(method = "readConfig", at = @At("HEAD"), remap = false)
    private static void phoenix$beforeRead(ConfigHolder<?> holder, CallbackInfo ci) {
        if (!phoenix$isOurs(holder)) return;
        // Always runs, regardless of the verbose-logging toggle - this is a real bug workaround, not diagnostics.
        ConfigFileSanitizer.normalizeLeadingTabs(ConfigIO.getConfigFile(holder));
        if (phoenix$verbose(holder)) {
            ConfigDiagnostics.logRawFile(ConfigIO.getConfigFile(holder), "BEFORE readConfig");
        }
    }

    @Inject(method = "readConfig", at = @At("RETURN"), remap = false)
    private static void phoenix$afterRead(ConfigHolder<?> holder, CallbackInfo ci) {
        if (!phoenix$isOurs(holder) || !phoenix$verbose(holder)) return;
        ConfigDiagnostics.logInMemoryArrays((ModConfig) holder.getConfigInstance(), "AFTER readConfig");
    }

    @Inject(method = "writeConfig", at = @At("HEAD"), remap = false)
    private static void phoenix$beforeWrite(ConfigHolder<?> holder, CallbackInfo ci) {
        if (!phoenix$isOurs(holder) || !phoenix$verbose(holder)) return;
        ConfigDiagnostics.logInMemoryArrays((ModConfig) holder.getConfigInstance(), "BEFORE writeConfig");
    }

    @Inject(method = "writeConfig", at = @At("RETURN"), remap = false)
    private static void phoenix$afterWrite(ConfigHolder<?> holder, CallbackInfo ci) {
        if (!phoenix$isOurs(holder) || !phoenix$verbose(holder)) return;
        ConfigDiagnostics.logRawFile(ConfigIO.getConfigFile(holder), "AFTER writeConfig");
    }

    private static boolean phoenix$isOurs(ConfigHolder<?> holder) {
        return holder != null && PhoenixChromaticCodes.MOD_ID.equals(holder.getConfigId());
    }

    private static boolean phoenix$verbose(ConfigHolder<?> holder) {
        Object instance = holder.getConfigInstance();
        return instance instanceof ModConfig cfg && cfg.compatibility.verboseConfigDiagnostics;
    }
}
