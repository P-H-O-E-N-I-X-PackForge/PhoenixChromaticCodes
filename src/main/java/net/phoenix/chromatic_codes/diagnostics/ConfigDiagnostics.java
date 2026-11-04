package net.phoenix.chromatic_codes.diagnostics;

import net.phoenix.chromatic_codes.config.ModConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.zip.CRC32;

/**
 * Temporary, verbose logging to track down a reported config-truncation/corruption bug: named codes
 * in phoenix_chromatic_codes.yaml allegedly getting cut short or duplicated across restarts.
 * <p>
 * Every call here is wrapped so that a failure in the logging itself can never break config loading.
 * This is meant to be removed once the bug is understood - it is not something a normal user needs.
 */
public final class ConfigDiagnostics {

    private static final Logger LOGGER = LogManager.getLogger("ChromaticCodes/ConfigDiagnostics");

    private ConfigDiagnostics() {}

    /** Dumps the raw bytes of {@code file} exactly as they are on disk right now. */
    public static void logRawFile(File file, String label) {
        try {
            LOGGER.info("==== [{}] RAW FILE @ {} ====", label, Instant.now());
            if (file == null || !file.exists()) {
                LOGGER.info("  (file does not exist: {})", file);
                LOGGER.info("==== end {} ====", label);
                return;
            }

            byte[] bytes = Files.readAllBytes(file.toPath());
            CRC32 crc = new CRC32();
            crc.update(bytes);

            LOGGER.info("  path={}", file.getAbsolutePath());
            LOGGER.info("  size={} bytes, crc32={}", bytes.length, Long.toHexString(crc.getValue()));

            // Decode leniently with REPLACE so a bad byte shows up as U+FFFD in the log
            // instead of throwing - we want to see exactly what's there, errors included.
            String text = new String(bytes, StandardCharsets.UTF_8);
            String[] lines = text.split("\n", -1);
            LOGGER.info("  lines={}", lines.length);
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].replace("\r", "\\r");
                LOGGER.info("  [{}] len={} | {}", i + 1, line.length(), line);
            }
            LOGGER.info("==== end {} ====", label);
        } catch (Throwable t) {
            LOGGER.error("ConfigDiagnostics.logRawFile failed for label '{}'", label, t);
        }
    }

    /** Dumps the in-memory contents of every long-value array field we care about right now. */
    public static void logInMemoryArrays(ModConfig cfg, String label) {
        try {
            LOGGER.info("==== [{}] IN-MEMORY ModConfig @ {} ====", label, Instant.now());
            if (cfg == null) {
                LOGGER.info("  (ModConfig.INSTANCE is null)");
                LOGGER.info("==== end {} ====", label);
                return;
            }

            logArray("customColors", cfg.colors.customColors);
            logArray("customGradients", cfg.colors.customGradients);
            logArray("namedColors", cfg.colors.namedColors);
            logArray("namedGradients", cfg.colors.namedGradients);
            LOGGER.info("==== end {} ====", label);
        } catch (Throwable t) {
            LOGGER.error("ConfigDiagnostics.logInMemoryArrays failed for label '{}'", label, t);
        }
    }

    private static void logArray(String name, String[] values) {
        if (values == null) {
            LOGGER.info("  {} = null", name);
            return;
        }
        LOGGER.info("  {} [{} entries]", name, values.length);
        for (int i = 0; i < values.length; i++) {
            String v = values[i];
            int len = v == null ? -1 : v.length();
            LOGGER.info("    [{}] len={} | {}", i, len, v);
        }
    }
}
