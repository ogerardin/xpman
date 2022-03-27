package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.exec.CommandExecutor;
import com.ogerardin.xplane.exec.ExecResults;
import com.ogerardin.xplane.util.platform.MacPlatform;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
@Slf4j
public class ToolUtils {

    /**
     * This method will in sequence:
     * <ol>
     *     <li>download a DMG file from the specified URL to a temporary file</li>
     *     <li>mount it</li>
     *     <li>look for a single app at the root of the mounted filesystem</li>
     *     <li>copy this app to the tools folder</li>
     *     <li>unmount the DMG and delete the temporary file</li>
     * </ol>
     */
    @SneakyThrows
    public static void installFromDmg(XPlane xPlane, String url) {
        Path tempFile = null;
        String mountPoint = null;
        try {
            tempFile = Files.createTempFile("", ".dmg");
            log.info("Downloading {} to {}", url, tempFile);
            FileUtils.copyURLToFile(new URL(url), tempFile.toFile());

            log.info("Attaching {}", tempFile);
            ExecResults results = CommandExecutor.exec("hdiutil", "attach", tempFile.toString());
            Pattern pattern = Pattern.compile("(.+)\\t(.+)\\t(.+)");
            mountPoint = results.getOutputLines().stream()
                    .map(pattern::matcher)
                    .filter(Matcher::matches)
                    .findFirst()
                    .map(matcher -> matcher.group(3))
                    .orElseThrow(() -> new RuntimeException("Failed to parse hdiutil output"));
            log.info("  mounted on {}", mountPoint);

            Path app = Files.list(Path.of(mountPoint))
                    .filter(MacPlatform.MacApp::isMacApp)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No .app found in DMG!"));
            log.info("Found app: {}", app);

            Path toolsFolder = xPlane.getPaths().tools();
            log.info("Copying app to {}", toolsFolder);
            FileUtils.copyDirectoryToDirectory(app.toFile(), toolsFolder.toFile());
        } finally {
            if (mountPoint != null) {
                log.info("Detaching {}", mountPoint);
                CommandExecutor.exec("hdiutil", "detach", "-force", mountPoint);
            }
            if (tempFile != null) {
                log.info("Deleting {}", tempFile);
                Files.deleteIfExists(tempFile);
            }
        }

    }

    public static void installFromZip(XPlane xPlane, String url) {

    }

    static Predicate<Path> hasString(String s) {
        return path -> {
            try {
                Path executable = new MacPlatform.MacApp(path).executable();
                return CommandExecutor.exec("fgrep", s, executable.toString()).getExitValue() == 0;
            } catch (IOException | InterruptedException | ConfigurationException e) {
                log.warn(e.toString());
                return false;
            }
        };
    }

    static Predicate<Path> hasName(String name) {
        return path -> path.getFileName().toString().equals(name);
    }
}
