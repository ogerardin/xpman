package com.ogerardin.xplane.skunkcrafts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Parsed Skunkcrafts updater configuration file (skunkcrafts_updater.cfg).
 *
 * <p>Format: pipe-delimited key|value lines. Known keys: zone, module, name, version,
 * locked, disabled, liveries.
 */
public record SkunkcraftsConfig(
        String name,
        String version,
        String moduleUrl,
        String zone,
        boolean locked,
        boolean disabled,
        boolean liveries
) {

    public static SkunkcraftsConfig parse(Path cfgFile) throws IOException {
        List<String> lines = Files.readAllLines(cfgFile);

        String name = null;
        String version = null;
        String moduleUrl = null;
        String zone = null;
        boolean locked = false;
        boolean disabled = false;
        boolean liveries = true;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

            String[] parts = trimmed.split("\\|", 2);
            if (parts.length < 2) continue;

            String key = parts[0].trim().toLowerCase();
            String value = parts[1].trim();

            switch (key) {
                case "name" -> name = value;
                case "version" -> version = value;
                case "module" -> moduleUrl = value;
                case "zone" -> zone = value;
                case "locked" -> locked = Boolean.parseBoolean(value);
                case "disabled" -> disabled = Boolean.parseBoolean(value);
                case "liveries" -> liveries = Boolean.parseBoolean(value);
            }
        }

        return new SkunkcraftsConfig(name, version, moduleUrl, zone, locked, disabled, liveries);
    }
}
