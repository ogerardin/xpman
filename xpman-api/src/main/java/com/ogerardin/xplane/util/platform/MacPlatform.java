package com.ogerardin.xplane.util.platform;

import com.ogerardin.xplane.util.exec.CommandExecutor;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.plist.XMLPropertyListConfiguration;
import org.apache.commons.lang.StringUtils;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Slf4j
public class MacPlatform implements Platform {

    @Getter
    public final int osType = com.sun.jna.Platform.MAC;

    @SneakyThrows
    @Override
    public void reveal(@NonNull Path path) {
        // if path is a directory, use any contained file otherwise the Finder will not select the directory
        // (except if it's an app)
        if (Files.isDirectory(path) && ! AppBundle.isAppBundle(path)) {
            try (Stream<Path> pathStream = Files.list(path)) {
                path = pathStream.findFirst().orElse(path);
            }
        }
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
            return new XMLPropertyListConfiguration(plistFile.toFile());

        }

        public static boolean isAppBundle(Path app) {
            return Files.isDirectory(app) && app.getFileName().toString().endsWith(".app");
        }
    }
}
