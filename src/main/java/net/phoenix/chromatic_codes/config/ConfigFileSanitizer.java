package net.phoenix.chromatic_codes.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Works around a bug in the third-party Configuration library we use for phoenix_chromatic_codes.yaml:
 * its YamlFormat.processMap only recognises a line as still belonging to the current nested section
 * when it starts with exactly two literal space characters. A line indented with a tab instead of
 * spaces (e.g. hand-typed in an editor that inserts tabs) fails that check, which makes the parser
 * think the whole "colors:" section ended right there - silently discarding everything after it in
 * that section (including unrelated sibling arrays) and replacing it with hardcoded defaults on the
 * very next save.
 * <p>
 * We can't fix the library itself, so instead we normalize our own config file - converting any
 * leading tab characters to spaces - right before the library ever reads it.
 */
public final class ConfigFileSanitizer {

    private static final Logger LOGGER = LogManager.getLogger("ChromaticCodes/ConfigFileSanitizer");

    /** How many spaces one leading tab is expanded to. Matches the 4-space indent our own array entries use. */
    private static final int TAB_WIDTH = 4;

    private ConfigFileSanitizer() {}

    /**
     * Replaces leading tab characters with spaces in {@code file}, rewriting it only if something
     * actually changed. Safe to call on a file that's already clean - it's then a no-op.
     */
    public static void normalizeLeadingTabs(File file) {
        try {
            if (file == null || !file.exists()) return;

            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            boolean changed = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                int leadingTabs = 0;
                while (leadingTabs < line.length() && line.charAt(leadingTabs) == '\t') leadingTabs++;
                if (leadingTabs == 0) continue;

                changed = true;
                lines.set(i, " ".repeat(leadingTabs * TAB_WIDTH) + line.substring(leadingTabs));
            }

            if (!changed) return;

            LOGGER.warn("Found tab-indented line(s) in {} - a known bug in the config library treats those as " +
                    "ending the section they're in, silently dropping everything after them. Converting tabs " +
                    "to spaces before reading.", file.getAbsolutePath());
            Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
        } catch (Throwable t) {
            LOGGER.error("ConfigFileSanitizer.normalizeLeadingTabs failed for {}", file, t);
        }
    }
}
