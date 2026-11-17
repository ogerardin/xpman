package com.ogerardin.xplane.aircraft.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.install.inspections.XPlaneVersionAtLeastInspection;
import com.ogerardin.xplane.install.types.AircraftInstallableType;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xplane.util.Urls;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

/**
 * Specialized {@link Aircraft} for the Calibra Simulations A330-200.
 *
 * <p>Reads the real version from the developer's {@code version.txt} (the ACF's {@code acf/_version}
 * is a placeholder the developer does not keep in sync with the skunkcrafts channel).
 * Recognition uses {@code acf/_name} and {@code acf/_author}; it deliberately avoids {@code acf/_studio}
 * because the published ACF ships with {@code acf/_studio = "Laminar Research"}.</p>
 *
 * <p>Installs are blocked when the running X-Plane is older than 12.4.4 (see
 * {@link CalibraA330InstallableType}).</p>
 */
@SuppressWarnings({"unused", "java:S2160"}) // Identity is deliberately ACF-file based (inherited); version cache must not affect it.
@Slf4j
public class CalibraA330 extends Aircraft {

    private static final String MIN_XPLANE_VERSION = "12.4.4";
    private static final Pattern VERSION_LINE = Pattern.compile("(?m)^version\\s+(.+)$");

    @Getter(lazy = true)
    private final String version = loadVersion();

    public CalibraA330(XPlane xPlane, AcfFile acfFile) throws InstantiationException {
        super(xPlane, acfFile, "A330-200 by Calibra Simulations");
        // acf/_author carries "A330-200: Calibra Simulations"; _studio ships as "Laminar Research".
        require(getAcfName().equals("Airbus A330-200") && contains(getAuthor(), "Calibra Simulations"));
    }

    @Override
    public boolean isAcfNameEditable() {
        return false;
    }

    @SneakyThrows
    private String loadVersion() {
        Path versionFile = getAcfFile().getFile().resolveSibling("version.txt");
        if (!Files.exists(versionFile)) {
            return super.getVersion();
        }
        try {
            String content = Files.readString(versionFile);
            Matcher matcher = VERSION_LINE.matcher(content);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (IOException e) {
            log.warn("Failed to read version from {}", versionFile, e);
        }
        return super.getVersion();
    }

    private static boolean contains(String haystack, String needle) {
        return haystack != null && haystack.contains(needle);
    }

    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.merge(super.getLinks(), Maps.mapOf(
                "A330-200 on calibrasim.com", Urls.url("https://calibrasim.com/a330-200/"),
                "Release notes", Urls.url("https://calibrasim.com/a330-200/release-notes/"),
                "Support on X-Plane.org", Urls.url("https://forums.x-plane.org/forums/topic/352123-a330-200-by-calibra-simulations-freeware-x-plane-12-teaser/"),
                "Report a bug", Urls.url("https://github.com/CalibraNR/CalibraSim_Support/issues/new/choose"),
                "Discord", Urls.url("https://discord.gg/JMG3cGF5rX")
        ));
    }

    /**
     * Installable type for the Calibra Simulations A330-200.
     *
     * <p>Recognizes archives that look like an aircraft and whose {@code version.txt} mentions
     * "Calibra Simulations", and rejects them when the installed X-Plane is older than 12.4.4.
     * Wins type detection over the generic {@link AircraftInstallableType} by inheritance depth.</p>
     */
    public static class CalibraA330InstallableType extends AircraftInstallableType {

        private static final String MARKER = "A330-200 by Calibra Simulations";
        private static final String VERSION_FILE = "version.txt";

        @Override
        public String description() {
            return "Calibra Simulations A330-200";
        }

        @Override
        public boolean recognizes(Archive archive) {
            if (!super.recognizes(archive)) {
                return false;
            }
            return archive.getPaths().stream()
                    .filter(p -> p.getFileName().toString().equals(VERSION_FILE))
                    .findFirst()
                    .map(p -> {
                        try {
                            return archive.getAsText(p).contains(MARKER);
                        } catch (IOException e) {
                            log.warn("Failed to read {} from archive", VERSION_FILE, e);
                            return false;
                        }
                    })
                    .orElse(false);
        }

        @Override
        public InspectionResult preconditions(XPlane xPlane, Archive archive) {
            return super.preconditions(xPlane, archive)
                    .append(new XPlaneVersionAtLeastInspection(xPlane, MIN_XPLANE_VERSION).inspect(archive));
        }
    }
}
