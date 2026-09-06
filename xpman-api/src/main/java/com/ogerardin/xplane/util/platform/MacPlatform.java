package com.ogerardin.xplane.util.platform;

import com.ogerardin.xplane.util.exec.CommandExecutor;
import com.ogerardin.xplane.util.exec.ExecResults;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.plist.XMLPropertyListConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Getter
@Slf4j
public class MacPlatform implements Platform {

    public final int osType = com.sun.jna.Platform.MAC;

    @SneakyThrows
    @Override
    public void reveal(@NonNull Path path) {
        CommandExecutor.exec("open", "-R", path.toString());
    }

    @Override
    public String revealLabel() {
        return "Reveal in Finder";
    }

    @SneakyThrows
    @Override
    public void openUrl(@NonNull URL url) {
        CommandExecutor.exec("open", url.toString());
    }

    /**
     * Start an application from the specified path.
     */
    @Override
    @SneakyThrows
    public void startApp(@NonNull Path app) {
        CommandExecutor.exec("open", app.toString());
    }

    @Override
    public boolean isRunnable(@NonNull Path path) {
        return AppBundle.isAppBundle(path);
    }

    @Override
    @SneakyThrows
    public void openFile(@NonNull Path file) {
        CommandExecutor.exec("open", file.toString());
    }

    @Override
    @SneakyThrows
    public String getVersion(Path appPath) {
        return new AppBundle(appPath).version();
    }

    // ponytail: heuristic version extraction via string scanning on raw Mach-O binary.
    // No proper Mach-O parser available; upgrade to one if accuracy becomes critical.
    private static final Pattern VERSION_PATTERN = Pattern.compile("\\b(\\d+\\.\\d+(?:\\.\\d+){0,2})\\b");

    @Override
    @SneakyThrows
    public String extractPluginVersion(Path xplFile) {
        byte[] bytes = Files.readAllBytes(xplFile);
        String content = new String(bytes);
        return findVersionNearPluginName(content, xplFile);
    }

    private String findVersionNearPluginName(String content, Path xplFile) {
        String folderName = xplFile.getParent().getFileName().toString();
        if (folderName.endsWith("64") || folderName.endsWith("32")) {
            folderName = folderName.substring(0, folderName.length() - 2);
        }

        int namePos = content.indexOf(folderName);
        if (namePos < 0) return null;

        int searchEnd = Math.min(content.length(), namePos + 500);
        String searchArea = content.substring(namePos, searchEnd);
        Matcher m = VERSION_PATTERN.matcher(searchArea);
        return m.find() ? m.group(1) : null;
    }

    @Override
    public List<Path> getCandidateInstallBaseFolders(Path userHome) {
        List<Path> bases = new ArrayList<>();
        bases.add(Paths.get("/Applications"));
        bases.add(userHome.resolve("Applications"));
        bases.add(userHome.resolve("Desktop"));
        bases.add(userHome);
        Path volumes = Paths.get("/Volumes");
        if (Files.isDirectory(volumes)) {
            try (Stream<Path> s = Files.list(volumes)) {
                s.forEach(bases::add);
            } catch (IOException e) {
                log.warn("Failed to list /Volumes", e);
            }
        }
        bases.add(userHome.resolve("Library/Application Support/Steam/steamapps/common"));
        return bases;
    }

    @SneakyThrows
    @Override
    public String getCpuType() {
        ExecResults exec = CommandExecutor.exec("sysctl", "-n", "machdep.cpu.brand_string");
        return exec.outputLines().get(0);
    }

    @SneakyThrows
    @Override
    public int getCpuCount() {
        ExecResults exec = CommandExecutor.exec("sysctl", "-n", "hw.ncpu");
        return Integer.parseInt(exec.outputLines().get(0));
    }

    @Override
    public String pluginPathIdentifier() {
        return "mac";
    }

    /**
     * Represents a macOS application bundle
     * @see <a href="https://developer.apple.com/library/archive/documentation/CoreFoundation/Conceptual/CFBundles/Introduction/Introduction.html">Bundle Programming Guide</a>
     */
    public record AppBundle(Path app) {
        public AppBundle {
            if (!isAppBundle(app)) {
                throw new IllegalArgumentException(app.toString() + " is not a Mac app bundle");
            }
        }

        public Path contentsDir() {
            return app.resolve("Contents");
        }

        public Path macOsDir() {
            return contentsDir().resolve("MacOS");
        }

        public Path executable() throws ConfigurationException {
            String executable = plist().getString("CFBundleExecutable");
            return macOsDir().resolve(executable);
        }

        @SneakyThrows
        public String version() {
            String version = plist().getString("CFBundleShortVersionString");
            return StringUtils.isBlank(version) ? null : version;
        }

        public XMLPropertyListConfiguration plist() throws ConfigurationException {
            Path plistFile = contentsDir().resolve("info.plist");
            XMLPropertyListConfiguration config = new XMLPropertyListConfiguration();
            new FileHandler(config).load(plistFile.toFile());
            return config;
        }

        public static boolean isAppBundle(Path app) {
            return Files.isDirectory(app) && app.getFileName().toString().endsWith(".app");
        }
    }
}
