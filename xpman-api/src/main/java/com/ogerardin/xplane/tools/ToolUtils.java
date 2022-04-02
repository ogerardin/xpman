package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.ProgressListener;
import com.ogerardin.xplane.util.exec.CommandExecutor;
import com.ogerardin.xplane.util.exec.ExecResults;
import com.ogerardin.xplane.util.platform.MacPlatform;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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
    public static void installFromDmg(XPlane xPlane, String url, ProgressListener progressListener) {
        Path tempFile = null;
        String mountPoint = null;
        Exception exception = null;
        try {
            progressListener.progress(0.0, "Downloading DMG");
            tempFile = Files.createTempFile("", ".dmg");
            progressListener.output("Downloading " + url + " to " + tempFile);
            FileUtils.copyURLToFile(new URL(url), tempFile.toFile());

            progressListener.progress(0.50, "Mounting DMG");
            progressListener.output("Attaching " + tempFile);
            ExecResults results = exec(progressListener, "hdiutil", "attach", tempFile.toString());
            Pattern pattern = Pattern.compile("(.+)\\t(.+)\\t(.+)");
            mountPoint = results.getOutputLines().stream()
                    .map(pattern::matcher)
                    .filter(Matcher::matches)
                    .findFirst()
                    .map(matcher -> matcher.group(3))
                    .orElseThrow(() -> new RuntimeException("Failed to parse hdiutil output"));
            progressListener.output("  mounted on " + mountPoint);

            Path app = Files.list(Path.of(mountPoint))
                    .filter(MacPlatform.MacApp::isMacApp)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No .app found in DMG!"));
            progressListener.output("Found app: " + app);

            progressListener.progress(0.70, "Copying app to tools folder");
            Path toolsFolder = xPlane.getPaths().tools();
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
                exec(progressListener,"hdiutil", "detach", "-force", mountPoint);
            }
            if (tempFile != null) {
                progressListener.output("Deleting " + tempFile);
                Files.deleteIfExists(tempFile);
            }

            progressListener.output("Done!");
            progressListener.progress(1.00, exception!= null ? "Failed" : "Completed");
        }

    }

    public static void installFromZip(XPlane xPlane, String url, ProgressListener progressListener) {

    }

    static Predicate<Path> hasString(String s) {
        return path -> {
            try {
                Path executable = new MacPlatform.MacApp(path).executable();
                return CommandExecutor.exec("fgrep", s, executable.toString()).getExitValue() == 0;
            } catch (IOException | InterruptedException | ConfigurationException e) {
                log.warn("fgrep failed: {}", e.toString());
                return false;
            }
        };
    }

    static Predicate<Path> hasName(String name) {
        return path -> path.getFileName().toString().equals(name);
    }

    static void defaultUninstaller(InstalledTool t, ProgressListener progressListener) {
        Exception exception = null;
        try {
            progressListener.progress(null, "Deleting...");

            File appFile = t.getApp().toFile();
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
     * {@link ExecResults#getOutputLines()} and {@link ExecResults#getErrorLines()} methods.
     * @return an {@link ExecResults} object containing the exit value and output of the command.
     */
    private static ExecResults exec(ProgressListener progressListener, String... args) throws IOException, InterruptedException {
        CommandExecutor executor = CommandExecutor.builder()
                .cmdarray(args)
                .outLineHandler(progressListener::output)
                .errLineHandler(progressListener::output)
                .build();
        ExecResults results = executor.exec();
        if (results.getExitValue() != 0) {
            throw new RuntimeException("Command returned non-zero exit status :" + Arrays.toString(args));
        }
        return results;
    }

}
