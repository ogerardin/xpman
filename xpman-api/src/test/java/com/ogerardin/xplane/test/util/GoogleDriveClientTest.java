package com.ogerardin.xplane.test.util;

import com.google.api.services.drive.model.File;
import com.ogerardin.xplane.util.GoogleDriveClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.empty;

/**
 * Integration tests for {@link GoogleDriveClient} against the live Zibo Mod Google Drive folder
 * ({@code 1RHz4PQqWNGGpVG9GaHr84kuGs8LM2xyK}).
 * <p>
 * The Drive folder was reorganized into {@code XP11}/{@code XP12} subfolders, so these tests
 * descend into the subfolders and pick the smallest downloadable file (~1.3 MB today).
 * <p>
 * Disabled by default because they require live network access and hit a real Google Drive folder
 * whose contents shift over time. Run on demand by removing {@code @Disabled} (or via the IDE).
 */
@Slf4j
class GoogleDriveClientTest {

    private static final String FOLDER_ID = "1RHz4PQqWNGGpVG9GaHr84kuGs8LM2xyK";
    private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

    private GoogleDriveClient client;

    @BeforeEach
    void setUp() throws GeneralSecurityException, IOException {
        client = new GoogleDriveClient();
    }

    @Test
    @Disabled("Live Google Drive integration. Run on demand by removing @Disabled.")
    void getFiles() throws GeneralSecurityException, IOException {
        List<File> files = client.getFiles(FOLDER_ID);
        assertThat(files, is(not(empty())));
        assertThat(files, everyItem(hasProperty("id", is(notNullValue()))));
    }

    @Test
    @Disabled("Live Google Drive integration: downloads the smallest file (~1.3 MB). Run on demand by removing @Disabled.")
    void download() throws GeneralSecurityException, IOException {
        File file = pickSmallestFile();
        long expectedSize = file.getSize();
        log.debug("Downloading {}, size {}", file.getName(), expectedSize);

        Path tempFile = Files.createTempFile("xpman-download", ".tmp");
        try {
            try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
                client.downloadFile(file.getId(), outputStream);
            }
            long actualSize = Files.size(tempFile);
            assertThat(actualSize, is(expectedSize));
            assertThat(actualSize, is(greaterThan(0L)));
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                log.warn("Failed to delete temp file {}", tempFile, e);
            }
        }
    }

    /**
     * Exercises the resumable-download path: download the first half of the file, then resume with
     * an HTTP {@code Range} header and assert the final byte count matches the Drive-reported size.
     */
    @Test
    @Disabled("Live Google Drive integration: downloads the smallest file twice (~2.6 MB total). Run on demand by removing @Disabled.")
    void download2() throws GeneralSecurityException, IOException {
        File file = pickSmallestFile();
        long totalSize = file.getSize();
        long half = totalSize / 2;

        Path tempFile = Files.createTempFile("xpman-resumable", ".part");
        try {
            // Pre-fetch the first half via a Range request to simulate an interrupted download.
            URL url = client.getDownloadUrl(file.getId());
            HttpURLConnection first = (HttpURLConnection) url.openConnection();
            first.setRequestProperty("Range", "bytes=0-" + (half - 1));
            first.connect();
            try (InputStream in = first.getInputStream();
                 OutputStream out = Files.newOutputStream(tempFile, CREATE)) {
                IOUtils.copy(in, out);
            }
            assertThat(Files.size(tempFile), is(half));

            // Resume: request bytes from {@code half} onward and append.
            HttpURLConnection resume = (HttpURLConnection) url.openConnection();
            resume.setRequestProperty("Range", "bytes=" + half + "-");
            resume.connect();
            try (InputStream in = resume.getInputStream();
                 OutputStream out = Files.newOutputStream(tempFile, APPEND)) {
                IOUtils.copy(in, out);
            }
            assertThat(Files.size(tempFile), is(totalSize));
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                log.warn("Failed to delete temp file {}", tempFile, e);
            }
        }
    }

    /**
     * Recursively lists all non-folder files under {@code folderId} (descends into subfolders).
     */
    private List<File> listAllFiles(String folderId) throws GeneralSecurityException, IOException {
        List<File> result = new ArrayList<>();
        for (File f : client.getFiles(folderId)) {
            if (FOLDER_MIME_TYPE.equals(f.getMimeType())) {
                result.addAll(listAllFiles(f.getId()));
            } else {
                result.add(f);
            }
        }
        return result;
    }

    /**
     * Returns the smallest downloadable (non-folder) file in the Zibo Drive tree, so downloads stay
     * fast (~1.3 MB currently).
     */
    private File pickSmallestFile() throws GeneralSecurityException, IOException {
        return listAllFiles(FOLDER_ID).stream()
                .min(Comparator.comparing(File::getSize))
                .orElseThrow();
    }
}