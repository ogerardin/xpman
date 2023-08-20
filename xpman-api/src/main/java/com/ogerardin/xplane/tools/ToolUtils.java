package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.util.exec.CommandExecutor;
import com.ogerardin.xplane.util.exec.ExecResults;
import com.ogerardin.xplane.util.platform.MacPlatform;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.progress.SubProgressListener;
import com.ogerardin.xplane.util.zip.ZipArchive;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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

    public static void install(@NonNull URL url, @NonNull Path toolsFolder, @NonNull ProgressListener progressListener) throws IOException, InterruptedException {
        String path = url.getPath();
        String ref = url.getRef();
        if (path.endsWith(".dmg") || (ref != null && ref.endsWith(".dmg"))) {
            installFromDmg(url, toolsFolder, progressListener);
        }
        else if (path.endsWith(".zip") || (ref != null && ref.endsWith(".zip"))) {
            installFromZip(url, toolsFolder, progressListener);
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
            Pattern pattern = Pattern.compile("(.+)\\t(.+)\\t(.+)");
            mountPoint = results.outputLines().stream()
                    .map(pattern::matcher)
                    .filter(Matcher::matches)
                    .findFirst()
                    .map(matcher -> matcher.group(3))
                    .orElseThrow(() -> new RuntimeException("Failed to parse hdiutil output"));
            progressListener.output("  mounted on " + mountPoint);

            Path app = Files.list(Path.of(mountPoint))
                    .filter(MacPlatform.AppBundle::isAppBundle)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No .app found in DMG!"));
            progressListener.output("Found app: " + app);

            progressListener.progress(0.70, "Copying app to tools folder");
            progressListener.output("Copying " + app + " to " + toolsFolder);
            FileUtils.copyDirectoryToDirectory(app.toFile(), toolsFolder.toFile());

        }
        catch (Exception e) {
            exception = e;
            progressListener.output("Caught exception: " + e);
        }
        finally {
            if (mountPoint != null) {
                progressListener.output("Detaching " + mountPoint);
                try {
                    String finalMountPoint = mountPoint;
                    exec(progressListener, "hdiutil", "detach", "-force", mountPoint)
                            .or(res -> progressListener.output("Failed to unmount image from " + finalMountPoint));
                } catch (InterruptedException ignore) {
                }
            }
            if (tempFile != null) {
                progressListener.output("Deleting " + tempFile);
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    progressListener.output("Failed to delete temporary file " + tempFile);
                }
            }

            progressListener.output("Done!");
            progressListener.progress(1.00, exception!= null ? "Failed" : "Completed");
        }

    }

    public static void installFromZip(URL url, Path toolsFoder, ProgressListener progressListener) throws IOException {
        Path tempFile = null;
        Exception exception = null;
        try {
            progressListener.progress(0.0, "Downloading...");
            tempFile = Files.createTempFile(ToolUtils.class.getSimpleName(), ".zip");
            progressListener.output("Downloading " + url + " to " + tempFile);
            FileUtils.copyURLToFile(url, tempFile.toFile());

            // TODO handle cases where the ZIP doesn't contain a single executable
            progressListener.progress(0.50, "Extracting zip");
            progressListener.output("Extracting " + tempFile);
            ZipArchive zipArchive = new ZipArchive(tempFile);
            SubProgressListener subProgressListener = new SubProgressListener(progressListener, .51, 1.00);
            zipArchive.extract(toolsFoder, subProgressListener);

        }
        catch (Exception e) {
            exception = e;
            progressListener.output("Caught exception: " + e);
        }
        finally {
            if (tempFile != null) {
                progressListener.output("Deleting " + tempFile);
                Files.deleteIfExists(tempFile);
            }

            progressListener.output("Done!");
            progressListener.progress(1.00, exception!= null ? "Failed" : "Completed");
        }
    }

    static Predicate<Path> hasString(String s) {
        return path -> {
            try {
                //FIXME make it work for non-Mac platforms
                Path executable = new MacPlatform.AppBundle(path).executable();
                return CommandExecutor.exec("fgrep", s, executable.toString()).getExitValue() == 0;
            } catch (IOException | InterruptedException | ConfigurationException e) {
                log.warn("fgrep failed: {}", e.toString());
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
            progressListener.output("Deleting " + appFile);
            var fileUtils = com.sun.jna.platform.FileUtils.getInstance();
            fileUtils.moveToTrash(appFile);
        } catch (Exception e) {
            exception = e;
            progressListener.output("Caught exception: " + e);
        } finally {
            progressListener.output("Done!");
            progressListener.progress(1.00, exception!= null ? "Failed" : "Completed");
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
        ExecResults results = executor.exec();
        return results;
    }

}
