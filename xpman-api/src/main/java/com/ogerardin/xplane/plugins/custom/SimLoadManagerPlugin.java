package com.ogerardin.xplane.plugins.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.inspections.AssertXPlaneVersionAtLeast;
import com.ogerardin.xplane.install.inspections.MissingSystemLibrariesInspection;
import com.ogerardin.xplane.install.inspections.MissingSystemLibrariesInspection.SystemLibraryRequirement;
import com.ogerardin.xplane.install.types.PluginInstallableType;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xplane.util.Urls;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

/**
 * The standalone SimLoad Manager plugin (v5+), installed in Resources/plugins/SimLoadManager.
 */
@SuppressWarnings("unused")
@Slf4j
public class SimLoadManagerPlugin extends Plugin {

    private static final String FOLDER_NAME = "SimLoadManager";
    private static final String MIN_XPLANE_VERSION = "12.4.4";

    public SimLoadManagerPlugin(XPlane xPlane, Path xplFile) throws InstantiationException {
        super(xPlane, xplFile, "SimLoad Manager", "Realistic PAX, cargo & fuel ground operations");
        require(getBaseFolder(xplFile).getFileName().toString().equalsIgnoreCase(FOLDER_NAME));
    }

    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.mapOf(
                "Plugin page on X-Plane.org", Urls.url("https://forums.x-plane.org/files/file/93858-simload-manager-realistic-pax-cargo-fuel-ground-operations/"),
                "Ko-fi page", Urls.url("https://ko-fi.com/rackhamrpl"),
                "GitHub repository", Urls.url("https://github.com/Rackham-Sim/Simload-Manager")
        );
    }

    /**
     * Installable type for the SimLoad Manager plugin (v5+).
     * Recognizes archives containing .xpl files and a SimLoadManager folder.
     * Adds Linux libcurl requirement for SimBrief integration.
     */
    @SuppressWarnings("unused")
    public static class SimLoadManagerPluginInstallableType extends PluginInstallableType {

        @Override
        public String description() {
            return "SimLoad Manager plugin";
        }

        @Override
        public boolean recognizes(Archive archive) {
            // Check for SimLoadManager v5+ structure:
            // - A folder named "SimLoadManager"
            // - That contains a "lua" subfolder
            // - And contains .xpl files (in platform subfolder like mac_x64/)

            boolean hasSimLoadManagerFolder = archive.getPaths().stream()
                    .anyMatch(path -> {
                        String name = path.getFileName().toString();
                        return name.equals("SimLoadManager") && path.getNameCount() >= 1;
                    });

            boolean hasLuaFolder = archive.getPaths().stream()
                    .anyMatch(path -> {
                        String name = path.getFileName().toString();
                        return name.equals("lua") &&
                               path.toString().contains("SimLoadManager");
                    });

            boolean hasXplFiles = archive.getPaths().stream()
                    .anyMatch(path -> path.getFileName().toString().endsWith(".xpl"));

            return hasSimLoadManagerFolder && hasLuaFolder && hasXplFiles;
        }

        @Override
        public InspectionResult preconditions(XPlane xPlane, Archive archive) {
            return super.preconditions(xPlane, archive)
                    .append(new AssertXPlaneVersionAtLeast(xPlane, MIN_XPLANE_VERSION).inspect(archive))
                    .append(new MissingSystemLibrariesInspection(List.of(
                            new SystemLibraryRequirement(
                                    Platforms.LINUX,
                                    "libcurl.so.4",
                                    "Install via package manager (e.g., 'sudo apt install libcurl4' or 'sudo dnf install libcurl')"
                            )
                    )).inspect(archive));
        }
    }
}
