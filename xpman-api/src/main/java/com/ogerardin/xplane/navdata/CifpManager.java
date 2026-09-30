package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.util.exec.CommandExecutor;
import com.ogerardin.xplane.util.exec.ExecResults;
import com.ogerardin.xplane.util.platform.Platform;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import com.ogerardin.xplane.util.zip.ZipArchive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Installs FAA CIFP (ARINC 424) navigation data into an X-Plane installation.
 * Downloads and caches the third-party convert424toxplane converter, runs it over the
 * FAACIFP18 file found in the user's FAA cycle archive, and copies the results into
 * the Custom Data folder.
 */
@RequiredArgsConstructor
@Slf4j
public class CifpManager {

    /** The extensionless file the FAA ships the current cycle in. */
    private static final String FAACIFP18 = "FAACIFP18";

    /** Shared stem of every convert424toxplane executable, with or without a suffix. */
    private static final String CONVERTER_STEM = "convert424toxplane";

    /** Fixed HTTPS source for the converter, as a Dropbox folder zip download. */
    private static final URI CONVERTER_URL = URI.create(
        "https://www.dropbox.com/scl/fo/mnw9cufqcxgmkzpx35269/AG84gKEZWlR1Sk5Vld0csGk?rlkey=udqtjnhsdo0c7cnhbe2o0ft6o&dl=1"
    );

    private final XPlane xPlane;

    /**
     * Installs the FAA CIFP data found in the specified archive.
     *
     * @throws IOException          if the archive cannot be read or the files cannot be written
     * @throws InterruptedException if the converter process is interrupted
     */
    public void install(Archive archive, ProgressListener progress)
            throws IOException, InterruptedException {

        Path workingDir = Files.createTempDirectory("xpman-cifp");
        try {
            Path source = extractSource(archive, workingDir, progress);
            Path converter = resolveConverter(progress);

            progress.output("Running " + converter.getFileName());
            ExecResults results = CommandExecutor.builder()
                    .cmdarray(new String[] { converter.toString(), source.getFileName().toString(), "FMS" })
                    .dir(workingDir)
                    .outLineHandler(progress::output)
                    .errLineHandler(progress::output)
                    .build()
                    .exec();

            if (results.isSuccessful()) {
                copyConvertedData(workingDir, progress);
            } else {
                String details = "CIFP conversion failed (exit " + results.getExitValue() + ")";
                log.warn("{}: {}", details, String.join("\n", results.errorLines()));
                progress.output(details + "; earth_424.dat will still be installed but no CIFP folder.");
            }

            copyToCustomData(source, progress);
            xPlane.getNavDataManager().reload();
        } finally {
            FileUtils.deleteQuietly(workingDir.toFile());
        }
    }

    /**
     * Extracts the FAACIFP18 entry from the archive into the working directory and
     * renames it to the .dat name the converter expects.
     *
     * The entry's parent folder, if any, is used as the extraction subpath so a nested
     * FAACIFP18 still lands at the root of the working directory under its own name.
     */
    private Path extractSource(Archive archive, Path workingDir, ProgressListener progress) throws IOException {
        Path entry = archive.getPaths().stream()
                .filter(path -> FAACIFP18.equals(path.getFileName().toString()))
                .findFirst()
                .orElseThrow(() -> new IOException("FAACIFP18 not found in the archive"));

        Path parent = entry.getParent();
        if (parent == null) {
            archive.extract(workingDir, progress);
        } else {
            archive.extract(workingDir, parent, progress);
        }

        Path extracted = workingDir.resolve(FAACIFP18);
        if (!Files.exists(extracted)) {
            throw new IOException("FAACIFP18 could not be extracted from the archive");
        }
        Path source = workingDir.resolve("FAACIFP18.dat");
        Files.move(extracted, source, StandardCopyOption.REPLACE_EXISTING);
        return source;
    }

    /**
     * Copies the CIFP folder the converter generated into Custom Data, overwriting
     * any existing entries.
     */
    private void copyConvertedData(Path workingDir, ProgressListener progress) throws IOException {
        Path generated = workingDir.resolve("CIFP");
        if (!Files.isDirectory(generated)) {
            log.warn("CIFP converter produced no CIFP folder in {}", workingDir);
            progress.output("CIFP converter produced no CIFP folder; only earth_424.dat was installed.");
            return;
        }
        Path target = xPlane.getPaths().customData().resolve("CIFP");
        progress.output("Copying CIFP data to " + target);
        FileUtils.copyDirectory(generated.toFile(), target.toFile());
    }

    /**
     * Copies the source file to Custom Data as earth_424.dat.
     */
    private void copyToCustomData(Path source, ProgressListener progress) throws IOException {
        Path target = xPlane.getPaths().customData().resolve("earth_424.dat");
        progress.output("Installing " + target.getFileName());
        Files.createDirectories(target.getParent());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Returns the platform folder name inside the converter archive, or null if this
     * platform has no CIFP converter.
     */
    static String osFolderName(Platform platform) {
        return platform.cifpConverterFolder();
    }

    /**
     * Returns the converter binary name for the given platform and X-Plane version.
     */
    static String converterName(Platform platform, XPlaneMajorVersion majorVersion) {
        return platform.cifpConverterName(majorVersion);
    }

    /**
     * Resolves the converter binary, downloading and caching it if it is not already
     * present and runnable under {@code XPlane/Resources/tools/<os>/}.
     *
     * @throws IOException if the converter cannot be downloaded or extracted
     */
    Path resolveConverter(ProgressListener progress) throws IOException {
        Platform platform = Platforms.getCurrent();
        String os = osFolderName(platform);
        String name = converterName(platform, xPlane.getMajorVersion());
        if (os == null || name == null) {
            throw new IOException("No FAA CIFP converter is available for " + platform);
        }
        Path toolsFolder = xPlane.getPaths().tools().resolve(os);
        Path binary = toolsFolder.resolve(name);

        if (Files.isExecutable(binary) && platform.isRunnable(binary)) {
            log.debug("Using cached CIFP converter {}", binary);
            return binary;
        }

        progress.output("Downloading CIFP converter");
        Files.createDirectories(toolsFolder);

        Path tempZip = Files.createTempFile("xpman-cifp-converter", ".zip");
        try {
            try (InputStream in = CONVERTER_URL.toURL().openStream()) {
                FileUtils.copyInputStreamToFile(in, tempZip.toFile());
            }

            // The archive nests everything under an unknown top-level folder, so the
            // <os> subtree is located by scanning entry names rather than assumed.
            ZipArchive archive = new ZipArchive(tempZip);
            Path osRoot = findOsRoot(archive, os).orElseThrow(() -> new IOException(
                "CIFP converter archive contains no '" + os + "' folder"));
            log.debug("CIFP converter '{}' folder found at {} in the archive", os, osRoot);

            // Extract the whole <os> subtree so the Windows geoids folder, which must
            // sit next to the executable, is preserved. The subpath overload strips the
            // located root, discarding the unknown Dropbox prefix.
            archive.extract(toolsFolder, osRoot, progress);
        } finally {
            Files.deleteIfExists(tempZip);
        }

        if (!Files.exists(binary)) {
            throw new IOException("CIFP converter " + binary + " was not found in the archive");
        }
        if (platform.isQuarantined(binary)) {
            progress.output("Removing quarantine from " + binary.getFileName());
            platform.removeQuarantine(binary);
        }
        if (!platform.isRunnable(binary)) {
            throw new IOException("CIFP converter " + binary + " for " + os
                + " / X-Plane " + xPlane.getMajorVersion() + " is not runnable on this system");
        }
        return binary;
    }

    /**
     * Finds the archive-relative root folder of the {@code os} subtree, i.e. the entry
     * that is the parent of the first {@code <os>/convert424toxplane*} path.
     */
    private static Optional<Path> findOsRoot(Archive archive, String os) {
        return archive.getPaths().stream()
                .filter(path -> hasOsSegmentThenConverter(path, os))
                .map(Path::getParent)
                .min(Comparator.comparingInt(Path::getNameCount));
    }

    private static boolean hasOsSegmentThenConverter(Path path, String os) {
        return IntStream.range(0, path.getNameCount() - 1)
                .anyMatch(i -> path.getName(i).toString().equals(os)
                        && path.getName(i + 1).toString().toLowerCase(Locale.ROOT).startsWith(CONVERTER_STEM));
    }
}