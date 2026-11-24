package com.ogerardin.xplane.aircraft.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.aircraft.Livery;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.OpenUrlFix;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.inspection.TrashFolderFix;
import com.ogerardin.xplane.install.inspections.XPlaneVersionAtLeastInspection;
import com.ogerardin.xplane.install.types.AircraftInstallableType;
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
import java.util.ArrayList;
import java.util.List;
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
 *
 * <p>Warns when a livery requires an optional Carda engine mod that is not installed — the mod
 * ships as empty placeholder OBJs and the sim renders a "CARDA MOD MISSING" box for 30s on first
 * load.</p>
 */
@SuppressWarnings({"unused", "java:S2160"}) // Identity is deliberately ACF-file based (inherited); version cache must not affect it.
@Slf4j
public class CalibraA330 extends Aircraft {

    private static final String MIN_XPLANE_VERSION = "12.4.4";
    private static final Pattern VERSION_LINE = Pattern.compile("(?m)^version\\s+(.+)$");

    /**
     * The optional Carda engine mods. Each ships as a folder under the aircraft's {@code objects}
     * directory; when the user does not replace Calibra's shipped placeholders with Carda's real
     * OBJs, any livery tagged for that engine shows a "CARDA MOD MISSING" box for 30s on first load.
     */
    enum CardaEngineMod {
        GE("GE Engines", "GE CF6-80E1",
                "https://forum.thresholdx.net/files/file/567-general-electric-cf6-80e1-engine-mod-for-jar330/"),
        PW("PW Engines", "PW4000",
                "https://forum.thresholdx.net/files/file/948-pratt-whitney-4000-100-for-jar330/");

        /** Subfolder of {@code <aircraftFolder>/objects/} holding the engine OBJs. */
        final String folderName;

        /** Human-readable label used in the inspection message. */
        final String label;

        /** Threshold download page for Carda's real engine mod. */
        final String downloadUrl;

        CardaEngineMod(String folderName, String label, String downloadUrl) {
            this.folderName = folderName;
            this.label = label;
            this.downloadUrl = downloadUrl;
        }
    }

    /**
     * Marker string inside the placeholder OBJs Calibra ships with the aircraft. When any OBJ
     * inside a {@code CardaEngineMod} folder still contains this string, the mod is considered
     * missing (the user has not yet replaced the placeholder with Carda's real OBJs).
     */
    private static final String CARDA_PLACEHOLDER_MARKER = "CALIBRA_PLACEHOLDER";

    /**
     * Regex for engine tags embedded in a livery folder name, e.g. {@code [A332 GE] Air France F-GZCL}
     * or {@code [PW] Some Livery}. Tag is case-insensitive.
     */
    private static final Pattern LIVERY_ENGINE_TAG =
            Pattern.compile("\\[\\s*\\S*\\s*(GE|PW)\\s*]", Pattern.CASE_INSENSITIVE);

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

    @Override
    public InspectionResult inspect() {
        return super.inspect()
                .append(MissingCardaEnginesInspection.INSTANCE.inspect(this));
    }

    /**
     * Emits one {@link Severity#WARN} per livery that requires a missing Carda engine mod. A mod
     * is missing when its folder does not exist or still contains the {@link #CARDA_PLACEHOLDER_MARKER}
     * that Calibra ships in lieu of Carda's real OBJs. A livery is classified by the {@code GE} /
     * {@code PW} tag inside square brackets in its folder name (e.g. {@code [A332 GE] Air France F-GZCL}).
     * The message carries an {@link OpenUrlFix} pointing at Carda's Threshold download page.
     */
    public enum MissingCardaEnginesInspection implements Inspection<Aircraft> {
        INSTANCE;

        @SneakyThrows
        @Override
        public InspectionResult inspect(Aircraft target) {
            Path objectsDir = target.getAcfFile().getFile().resolveSibling("objects");
            // Cache per-folder presence so multiple liveries sharing a mod don't re-scan.
            Map<CardaEngineMod, Boolean> modPresent = new java.util.EnumMap<>(CardaEngineMod.class);
            for (CardaEngineMod mod : CardaEngineMod.values()) {
                modPresent.put(mod, isModPresent(objectsDir, mod));
            }

            List<InspectionMessage> messages = new ArrayList<>();
            for (Livery livery : target.getLiveries()) {
                Matcher m = LIVERY_ENGINE_TAG.matcher(livery.getName());
                if (!m.find()) continue;
                CardaEngineMod mod = CardaEngineMod.valueOf(m.group(1));
                if (modPresent.get(mod)) continue;
                Path liveryFolder = target.getLiveriesFolder().resolve(livery.getFolder());
                messages.add(InspectionMessage.builder()
                        .severity(Severity.WARN)
                        .object("livery " + livery.getName())
                        .message("requires the Carda " + mod.label + " engine mod, which is not installed")
                        .details(detailsFor(mod))
                        .fix(new OpenUrlFix(
                                "open Carda " + mod.label + " download page (requires login)",
                                mod.downloadUrl))
                        .fix(new TrashFolderFix(liveryFolder))
                        .build());
            }
            return InspectionResult.of(messages);
        }

        private static String detailsFor(CardaEngineMod mod) {
            return "Without Carda's " + mod.label + " engine mod, this livery shows a "
                    + "'CARDA MOD MISSING' box for 30 seconds in the sim. To use this livery, "
                    + "download and install the mod.";
        }

        private static boolean isModPresent(Path objectsDir, CardaEngineMod mod) {
            Path modDir = objectsDir.resolve(mod.folderName);
            if (!Files.isDirectory(modDir)) return false;
            try (var stream = Files.list(modDir)) {
                return stream
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".obj"))
                        .noneMatch(MissingCardaEnginesInspection::isPlaceholder);
            } catch (IOException e) {
                log.warn("Failed to inspect Carda mod folder {}", modDir, e);
                return true; // treat unreadable as present — do not warn
            }
        }

        @SneakyThrows
        private static boolean isPlaceholder(Path objFile) {
            String content = Files.readString(objFile);
            return content.contains(CARDA_PLACEHOLDER_MARKER);
        }
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
