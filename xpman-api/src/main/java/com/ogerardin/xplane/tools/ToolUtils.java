package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.util.exec.CommandExecutor;
import com.ogerardin.xplane.util.exec.ExecResults;
import com.ogerardin.xplane.util.platform.MacPlatform;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.progress.SubProgressListener;
import com.ogerardin.xplane.util.zip.ZipArchive;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A set of utility methods mainly related to installing or uninstalling tools.
 * TODO: currently tool installation is handled differently from other types of installations (aircraft, scenery)
 *  see package {@link com.ogerardin.xplane.install}; this should be unified.
 */
@UtilityClass
@Slf4j
public class ToolUtils {

    private static final String MSG_CAUGHT_EXCEPTION = "Caught exception: ";
    private static final String MSG_DELETING = "Deleting ";
    private static final String MSG_DONE = "Done!";
    private static final String MSG_FAILED = "Failed";
    private static final String MSG_COMPLETED = "Completed";

    @SuppressWarnings("java:S1130") // Platform helpers use @SneakyThrows for interruptible process execution; callers handle interruption.
    public static void install(@NonNull URL url, @NonNull Path toolsFolder, @NonNull Path file, @NonNull ProgressListener progressListener) throws IOException, InterruptedException {
        String path = url.getPath();
        String ref = url.getRef();
        if (path.endsWith(".dmg") || (ref != null && ref.endsWith(".dmg"))) {
            installFromDmg(url, toolsFolder, progressListener);
        }
        else if (path.endsWith(".zip") || (ref != null && ref.endsWith(".zip"))) {
            installFromZip(url, toolsFolder, file, progressListener);
        }
        else {
            throw new IllegalArgumentException("Unsupported URL: " + url);
        }
    }

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
    @SuppressWarnings("java:S5443") // Short-lived installer temp file; deleted after the app has been copied.
    public static void installFromDmg(URL url, Path toolsFolder, ProgressListener progressListener) throws IOException {
        Path tempFile = null;
        String mountPoint = null;
        Exception exception = null;
        try {
            progressListener.progress(0.0, "Downloading...");
            tempFile = Files.createTempFile(ToolUtils.class.getSimpleName(), ".dmg");
            progressListener.output("Downloading " + url + " to " + tempFile);
            FileUtils.copyURLToFile(url, tempFile.toFile());

            progressListener.progress(0.50, "Mounting DMG");
            progressListener.output("Attaching " + tempFile);
            ExecResults results = exec(progressListener, "hdiutil", "attach", tempFile.toString()).orThrow();
            Pattern pattern = Pattern.compile("([^\\t]+)\\t([^\\t]+)\\t(.+)");
            mountPoint = results.outputLines().stream()
                    .map(pattern::matcher)
                    .filter(Matcher::matches)
                    .findFirst()
                    .map(matcher -> matcher.group(3))
                    .orElseThrow(() -> new RuntimeException("Failed to parse hdiutil output"));
            progressListener.output("  mounted on " + mountPoint);

            Path app;
            try (var stream = Files.list(Path.of(mountPoint))) {
                app = stream
                        .filter(MacPlatform.AppBundle::isAppBundle)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No .app found in DMG!"));
            }
            progressListener.output("Found app: " + app);

            progressListener.progress(0.70, "Copying app to tools folder");
            progressListener.output("Copying " + app + " to " + toolsFolder);
            FileUtils.copyDirectoryToDirectory(app.toFile(), toolsFolder.toFile());

        }
        catch (InterruptedException e) {
            exception = e;
            Thread.currentThread().interrupt();
            progressListener.output(MSG_CAUGHT_EXCEPTION + e);
        }
        catch (Exception e) {
            exception = e;
            progressListener.output(MSG_CAUGHT_EXCEPTION + e);
        }
        finally {
            if (mountPoint != null) {
                progressListener.output("Detaching " + mountPoint);
                try {
                    String finalMountPoint = mountPoint;
                    exec(progressListener, "hdiutil", "detach", "-force", mountPoint)
                            .or(res -> progressListener.output("Failed to unmount image from " + finalMountPoint));
                } catch (InterruptedException _) {
                    Thread.currentThread().interrupt();
                }
            }
            if (tempFile != null) {
                progressListener.output(MSG_DELETING + tempFile);
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException _) {
                    progressListener.output("Failed to delete temporary file " + tempFile);
                }
            }

            progressListener.output(MSG_DONE);
            progressListener.progress(1.00, exception!= null ? MSG_FAILED : MSG_COMPLETED);
        }

    }

    @SuppressWarnings("java:S5443") // Short-lived installer temp file; deleted after extraction.
    public static void installFromZip(URL url, Path toolsFoder, Path file, ProgressListener progressListener) throws IOException {
        Path tempFile = null;
        Exception exception = null;
        try {
            progressListener.progress(0.0, "Downloading...");
            tempFile = Files.createTempFile(ToolUtils.class.getSimpleName(), ".zip");
            progressListener.output("Downloading " + url + " to " + tempFile);
            FileUtils.copyURLToFile(url, tempFile.toFile());

            progressListener.progress(0.50, "Extracting zip");
            progressListener.output("Extracting " + tempFile);
            ZipArchive zipArchive = new ZipArchive(tempFile);
            SubProgressListener subProgressListener = new SubProgressListener(progressListener, .51, 1.00);
            zipArchive.extract(toolsFoder, entryPath -> entryPath.equals(file) || entryPath.startsWith(file), subProgressListener);

        }
        catch (Exception e) {
            exception = e;
            progressListener.output(MSG_CAUGHT_EXCEPTION + e);
        }
        finally {
            if (tempFile != null) {
                progressListener.output(MSG_DELETING + tempFile);
                Files.deleteIfExists(tempFile);
            }

            progressListener.output(MSG_DONE);
            progressListener.progress(1.00, exception!= null ? MSG_FAILED : MSG_COMPLETED);
        }
    }

    static Predicate<Path> hasString(String s) {
        return path -> {
            try {
                String binary = new String(Files.readAllBytes(Platforms.getCurrent().getBinary(path)), StandardCharsets.ISO_8859_1);
                return binary.contains(s);
            } catch (IOException e) {
                log.warn("Failed to read binary {}: {}", path, e.toString());
                return false;
            }
        };
    }

    static Predicate<Path> hasName(String name) {
        return path -> path.endsWith(name);
    }

    static void defaultUninstaller(InstalledTool tool, ProgressListener progressListener) {
        Exception exception = null;
        try {
            progressListener.progress( "Deleting...");

            File appFile = tool.getApp().toFile();
            progressListener.output(MSG_DELETING + appFile);
            var fileUtils = com.sun.jna.platform.FileUtils.getInstance();
            fileUtils.moveToTrash(appFile);
        } catch (Exception e) {
            exception = e;
            progressListener.output(MSG_CAUGHT_EXCEPTION + e);
        } finally {
            progressListener.output(MSG_DONE);
            progressListener.progress(1.00, exception!= null ? MSG_FAILED : MSG_COMPLETED);
        }
    }

    /**
     * Utility method to run a command while logging the output to the specified progress listener using {@link ProgressListener#output}.
     * Standard output and error are logged to the same listener, but they are available separately in the return object's
     * {@link ExecResults#outputLines()} and {@link ExecResults#errorLines()} methods.
     * @return an {@link ExecResults} object containing the exit value and output of the command.
     */
    private static ExecResults exec(@NonNull ProgressListener progressListener, String... args) throws IOException, InterruptedException {
        CommandExecutor executor = CommandExecutor.builder()
                .cmdarray(args)
                .outLineHandler(progressListener::output)
                .errLineHandler(progressListener::output)
                .build();
        return executor.exec();
    }

}
