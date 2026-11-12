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

/**
 * macOS implementation of {@link Platform}, providing Mac-specific behaviors for file operations,
 * app launching, version extraction, and X-Plane installation discovery.
 * <p>
 * Key behaviors:
 * <ul>
 *   <li>Uses {@code open} for file/URL operations and {@code xattr} for quarantine management</li>
 *   <li>Parses Mach-O binaries heuristically to extract plugin versions (no proper Mach-O parser)</li>
 *   <li>Searches candidate install locations including {@code /Applications}, user home, {@code /Volumes},
 *       and Steam's common path</li>
 *   <li>Handles macOS app bundles via {@link AppBundle}, reading {@code Info.plist} for metadata</li>
 * </ul>
 */
@Getter
@Slf4j
public class MacPlatform implements Platform {

    @SuppressWarnings("java:S1170") // Platform.getOsType() is an instance method by interface contract.
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
        CommandExecutor.exec("open", app.toString()).orThrow();
    }

    @Override
    public boolean isRunnable(@NonNull Path path) {
        return AppBundle.isAppBundle(path);
    }

    @Override
    @SneakyThrows
    public void openFile(@NonNull Path file) {
        CommandExecutor.exec("open", file.toString()).orThrow();
    }

    @Override
    @SneakyThrows
    public void openInTextEditor(@NonNull Path file) {
        CommandExecutor.exec("open", "-t", file.toString()).orThrow();
    }

    @Override
    @SneakyThrows
    public String getVersion(Path appPath) {
        return new AppBundle(appPath).version();
    }

    @Override
    @SneakyThrows
    public Path getBinary(Path app) {
        return AppBundle.isAppBundle(app) ? new AppBundle(app).executable() : app;
    }

    // ponytail: heuristic version extraction via string scanning on raw Mach-O binary.
    // No proper Mach-O parser available; upgrade to one if accuracy becomes critical.
    private static final Pattern VERSION_PATTERN = Pattern.compile("\\b(\\d+\\.\\d+(?:\\.\\d+){0,2})\\b");

    private static final int MACH_O_MAGIC_64 = 0xFEEDFACF;     // thin 64-bit
    private static final int MACH_O_MAGIC_32 = 0xFEEDFACE;     // thin 32-bit (still a valid slice marker)
    private static final int MACH_O_CIGAM_64 = 0xCFFAEDFE;     // thin 64-bit, byte-flipped
    private static final int MACH_O_CIGAM_32 = 0xCEFAEDFE;
    private static final int FAT_MAGIC_BE = 0xCAFEBABE;
    private static final int FAT_CIGAM_BE = 0xBEBAFECA;
    private static final int FAT_ARCH_ENTRY_SIZE = 20;
    private static final int FAT_ARCH_COUNT_OFFSET = 4;
    private static final int FAT_ARCH_FIRST_ENTRY_OFFSET = 8;

    @Override
    @SneakyThrows
    public List<Architecture> getBinaryArchitectures(Path binary) {
        // Read a header big enough for the fat header + a handful of arch entries; cap memory
        // on a maliciously huge "fat" file. Anything under 8 bytes is not a Mach-O.
        byte[] header = new byte[(int) Math.min(Files.size(binary), 4096)];
        try (var in = Files.newInputStream(binary)) {
            int read = in.read(header);
            if (read < 8) return List.of();
        }

        int magic = readInt(header, 0);

        if (magic == MACH_O_CIGAM_64 || magic == MACH_O_CIGAM_32) {
            // Thin binary in non-native byte order: read cputype little-endian.
            return Architecture.ofMachOCpuType(readIntLE(header, 4))
                    .<List<Architecture>>map(List::of).orElse(List.of());
        }
        if (magic == MACH_O_MAGIC_64 || magic == MACH_O_MAGIC_32) {
            return Architecture.ofMachOCpuType(readInt(header, 4))
                    .<List<Architecture>>map(List::of).orElse(List.of());
        }
        if (magic == FAT_MAGIC_BE) {
            return readFatArchitectures(header, false);
        }
        if (magic == FAT_CIGAM_BE) {
            return readFatArchitectures(header, true);
        }
        return List.of();
    }

    private static List<Architecture> readFatArchitectures(byte[] header, boolean littleEndian) {
        if (header.length < FAT_ARCH_FIRST_ENTRY_OFFSET) return List.of();
        int nfat = littleEndian ? readIntLE(header, FAT_ARCH_COUNT_OFFSET)
                : readInt(header, FAT_ARCH_COUNT_OFFSET);
        // Bound the iteration: cap at the number of arch entries that fit in the header we read.
        int available = (header.length - FAT_ARCH_FIRST_ENTRY_OFFSET) / FAT_ARCH_ENTRY_SIZE;
        List<Architecture> result = new ArrayList<>();
        for (int i = 0; i < Math.min(nfat, available); i++) {
            int entryOffset = FAT_ARCH_FIRST_ENTRY_OFFSET + i * FAT_ARCH_ENTRY_SIZE;
            int cputype = littleEndian ? readIntLE(header, entryOffset) : readInt(header, entryOffset);
            Architecture.ofMachOCpuType(cputype).ifPresent(result::add);
        }
        return result;
    }

    private static int readInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24) | ((bytes[offset + 1] & 0xFF) << 16)
                | ((bytes[offset + 2] & 0xFF) << 8) | (bytes[offset + 3] & 0xFF);
    }

    private static int readIntLE(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8)
                | ((bytes[offset + 2] & 0xFF) << 16) | ((bytes[offset + 3] & 0xFF) << 24);
    }

    @Getter(lazy = true)
    private final Boolean appleSilicon = detectAppleSilicon();

    @SneakyThrows
    private boolean detectAppleSilicon() {
        String cpu = getCpuType();
        return cpu != null && cpu.startsWith("Apple");
    }

    @Override
    public boolean isAppleSilicon() {
        return getAppleSilicon();
    }

    @Override
    @SneakyThrows
    public String extractPluginVersion(Path xplFile) {
        byte[] bytes = Files.readAllBytes(xplFile);
        String content = new String(bytes);
        return findVersionNearPluginName(content, xplFile);
    }

    private String findVersionNearPluginName(String content, Path xplFile) {
        Path folder = xplFile.getParent();
        String folderName = folder.getFileName().toString();
        if (folderName.endsWith("64") || folderName.endsWith("32")) {
            folder = folder.getParent();
            folderName = folder.getFileName().toString();
        }

        // Search for plugin name followed by version pattern anywhere in binary
        Pattern pluginVersionPattern = Pattern.compile(
            Pattern.quote(folderName) + "[^\\d]{0,50}(\\d+\\.\\d+(?:\\.\\d+){0,2})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher m = pluginVersionPattern.matcher(content);
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

    @Override
    public Path getLogDir(Path userHome) {
        return userHome.resolve("Library/Logs/XPman");
    }

    @SneakyThrows
    @Override
    public String getCpuType() {
        ExecResults exec = CommandExecutor.exec("sysctl", "-n", "machdep.cpu.brand_string");
        return exec.outputLines().getFirst();
    }

    @SneakyThrows
    @Override
    public int getCpuCount() {
        ExecResults exec = CommandExecutor.exec("sysctl", "-n", "hw.ncpu");
        return Integer.parseInt(exec.outputLines().getFirst());
    }

    @Override
    public String pluginPathIdentifier() {
        return "mac";
    }

    @Override
    @SneakyThrows
    public boolean isQuarantined(Path path) {
        ExecResults results = CommandExecutor.exec("xattr", "-p", "com.apple.quarantine", path.toString());
        return results.isSuccessful();
    }

    @Override
    @SneakyThrows
    public void removeQuarantine(Path path) {
        CommandExecutor.exec("xattr", "-cr", path.toString());
    }

    @SneakyThrows
    @Override
    public void fixAppBundlePermissions(Path path) {
        if (!AppBundle.isAppBundle(path)) {
            return;
        }
        AppBundle bundle = new AppBundle(path);
        Path executable = bundle.executable();
        if (Files.exists(executable)) {
            CommandExecutor.exec("chmod", "+x", executable.toString());
        }
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
