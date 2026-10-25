package com.ogerardin.xplane.plugins.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.InstallationException;
import com.ogerardin.xplane.install.inspections.ConflictingPluginsInspection;
import com.ogerardin.xplane.install.inspections.SkunkcraftsUpdatableInspection;
import com.ogerardin.xplane.install.inspections.WouldOverwriteInspection;
import com.ogerardin.xplane.install.inspections.WouldOverwriteSubpathInspection;
import com.ogerardin.xplane.install.types.PluginInstallableType;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xplane.util.Urls;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

/**
 * The openSAM plugin (any version), installed in {@code Resources/plugins/openSAM}. Its release
 * archive also ships the openSAM_Library scenery library: installing extracts both halves —
 * the plugin into {@code Resources/plugins}, the library into {@code Custom Scenery} with its
 * scenery_packs.ini entry positioned above global airports and the legacy SAM_Library — and
 * uninstalling removes both.
 */
@SuppressWarnings("unused")
@Slf4j
public class OpenSamPlugin extends Plugin {

    private static final String FOLDER_NAME = "openSAM";
    static final String LIBRARY_FOLDER_NAME = "openSAM_Library";
    // openSAM/planes.cfg is the v5 marker: v4 and earlier ship acf_*.txt files instead
    private static final Path V5_MARKER = Path.of("openSAM", "planes.cfg");

    public OpenSamPlugin(XPlane xPlane, Path xplFile) throws InstantiationException {
        super(xPlane, xplFile, "openSAM", "Marshaller and VDGS guidance for all sceneries, jetway and object animation");
        require(getBaseFolder(xplFile).getFileName().toString().equalsIgnoreCase(FOLDER_NAME));
    }

    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.mapOf(
                "GitHub repository", Urls.url("https://github.com/hotbso/openSAM"),
                "Support (Discord)", Urls.url("https://discord.gg/BtDvKcUSEP")
        );
    }

    /**
     * Installable type for openSAM v5+ (X-Plane 12 only). Recognizes openSAM's two-root archive
     * — the plugin and its companion openSAM_Library scenery library — by the v5 marker
     * {@code openSAM/planes.cfg}, and installs both halves. Overrides preconditions to drop the
     * generic single-root-folder check: the archive legitimately has two root folders plus
     * loose distribution docs.
     */
    @SuppressWarnings("unused")
    public static class OpenSamPluginInstallableType extends PluginInstallableType {

        @Override
        public String description() {
            return "openSAM plugin (v5+)";
        }

        @Override
        public boolean recognizes(Archive archive) {
            return hasPluginRoot(archive) && hasLibraryRoot(archive) && hasV5Marker(archive);
        }

        @Override
        public InspectionResult preconditions(XPlane xPlane, Archive archive) {
            if (xPlane == null) {
                return SkunkcraftsUpdatableInspection.INSTANCE.inspect(archive);
            }
            return SkunkcraftsUpdatableInspection.INSTANCE
                    .and(new WouldOverwriteInspection(xPlane.getPluginManager()))
                    .and(libraryOverwriteInspection(xPlane))
                    .and(new ConflictingPluginsInspection(xPlane, conflictingPlugins()))
                    .inspectable(archive)
                    .inspect()
                    .append(versionGate(xPlane, archive))
                    .append(sceneryIniInspection(xPlane));
        }

        @Override
        public void install(XPlane xPlane, Archive archive, ProgressListener progress) throws InstallationException {
            try {
                xPlane.getPluginManager().install(archive, progress);
                Path libraryFolder = xPlane.getPaths().customScenery().resolve(LIBRARY_FOLDER_NAME);
                archive.extract(libraryFolder, Path.of(LIBRARY_FOLDER_NAME), progress);
                // the library must rank above global airports (XP12 standard jetway management)
                // and above the legacy SAM_Library when that is still installed
                xPlane.getSceneryManager().addToIniAbove(LIBRARY_FOLDER_NAME, "Global Airports", "SAM_Library");
            } catch (IOException e) {
                throw new InstallationException(e);
            }
        }

        /** Plugin base folder name → why it conflicts with openSAM. */
        protected Map<String, String> conflictingPlugins() {
            return Map.of(
                    "AutoDGS", "AutoDGS functionality is included in openSAM; remove the AutoDGS plugin",
                    "SAM", "the SAM plugin conflicts with openSAM; remove it (SAM_Library may remain in Custom Scenery)",
                    "SAM Seasons emulator", "its functionality is included in openSAM; remove it"
            );
        }

        /**
         * The version gate: openSAM v5+ runs on X-Plane 12 only. An undetectable X-Plane version
         * skips the check (house philosophy: a missing probe never blocks).
         */
        protected InspectionResult versionGate(XPlane xPlane, Archive archive) {
            if (xPlane.getVersion() == null) {
                return InspectionResult.empty();
            }
            if (xPlane.getMajorVersion() == XPlaneMajorVersion.XP12) {
                return InspectionResult.empty();
            }
            return InspectionResult.of(InspectionMessage.builder()
                    .severity(Severity.ERROR)
                    .message("openSAM v5+ runs on X-Plane 12 only")
                    .details("Use an openSAM 4.x release on X-Plane 11.")
                    .abort(true)
                    .build());
        }

        private Inspection libraryOverwriteInspection(XPlane xPlane) {
            return new WouldOverwriteSubpathInspection(
                    xPlane.getPaths().customScenery().resolve(LIBRARY_FOLDER_NAME), Path.of(LIBRARY_FOLDER_NAME));
        }

        /**
         * openSAM_Library must rank above global airports in scenery_packs.ini, which cannot be
         * ensured when the ini does not exist yet — X-Plane creates it on first run, appending
         * new sceneries at the bottom.
         */
        private InspectionResult sceneryIniInspection(XPlane xPlane) {
            if (Files.exists(xPlane.getPaths().customScenery().resolve("scenery_packs.ini"))) {
                return InspectionResult.empty();
            }
            return InspectionResult.of(InspectionMessage.warn(
                    "scenery_packs.ini not found: openSAM_Library cannot be positioned above global airports; "
                            + "check its rank in the Scenery panel after X-Plane's first run"));
        }
    }

    /**
     * Installable type for openSAM v4 and earlier — same two-root archive layout, recognized by
     * the ABSENCE of the v5 marker. These releases run on X-Plane 11 and 12 via separate
     * flavors carried only by the archive filename ({@code openSAM-XP11-v4.5.5.zip} /
     * {@code openSAM-XP12-v4.5.5.zip}); installing the wrong flavor is blocked when the flavor
     * is detectable.
     */
    @SuppressWarnings("unused")
    public static class OpenSam4PluginInstallableType extends OpenSamPluginInstallableType {

        @Override
        public String description() {
            return "openSAM plugin (v4 or earlier)";
        }

        @Override
        public boolean recognizes(Archive archive) {
            return hasPluginRoot(archive) && hasLibraryRoot(archive) && !hasV5Marker(archive);
        }

        @Override
        protected Map<String, String> conflictingPlugins() {
            return Map.of(
                    "SAM", "the SAM plugin conflicts with openSAM; remove it (SAM_Library may remain in Custom Scenery)",
                    "SAM Seasons emulator", "its functionality is included in openSAM; remove it"
            );
        }

        @Override
        protected InspectionResult versionGate(XPlane xPlane, Archive archive) {
            if (xPlane.getVersion() == null) {
                return InspectionResult.empty();
            }
            XPlaneMajorVersion flavor = flavorOf(archive.getSourcePath());
            if (flavor == null || flavor == xPlane.getMajorVersion()) {
                return InspectionResult.empty();
            }
            return InspectionResult.of(InspectionMessage.builder()
                    .severity(Severity.ERROR)
                    .message("This is the X-Plane %d flavor of openSAM but the selected installation is X-Plane %s"
                            .formatted(flavor.getMajor(), xPlane.getVersion()))
                    .details("openSAM releases up to v4 ship separate XP11 and XP12 flavors that do not work "
                            + "on the other X-Plane; download the matching flavor.")
                    .abort(true)
                    .build());
        }

        /** The XP11/XP12 release flavor carried by the archive filename, or null if undetectable. */
        public static XPlaneMajorVersion flavorOf(Path sourcePath) {
            String name = sourcePath.getFileName().toString().toUpperCase();
            if (name.contains("XP11")) {
                return XPlaneMajorVersion.XP11;
            }
            if (name.contains("XP12")) {
                return XPlaneMajorVersion.XP12;
            }
            return null;
        }
    }

    // archive recognition helpers, shared by both installable types

    /** True if the archive has the openSAM plugin root: a {@code .xpl} binary under an {@code openSAM/} folder. */
    static boolean hasPluginRoot(Archive archive) {
        return archive.getPaths().stream().anyMatch(path ->
                path.getNameCount() >= 2
                        && path.getName(0).toString().equalsIgnoreCase(FOLDER_NAME)
                        && path.getFileName().toString().toLowerCase().endsWith(".xpl"));
    }

    /** True if the archive has the openSAM_Library scenery library root (its library.txt). */
    static boolean hasLibraryRoot(Archive archive) {
        return archive.getPaths().stream().anyMatch(path ->
                path.getNameCount() >= 2
                        && path.getName(0).toString().equalsIgnoreCase(LIBRARY_FOLDER_NAME)
                        && path.getFileName().toString().equals("library.txt"));
    }

    static boolean hasV5Marker(Archive archive) {
        return archive.getPaths().stream().anyMatch(path ->
                path.getNameCount() == 2
                        && path.getName(0).toString().equalsIgnoreCase(FOLDER_NAME)
                        && path.getFileName().toString().equals(V5_MARKER.getFileName().toString()));
    }
}
