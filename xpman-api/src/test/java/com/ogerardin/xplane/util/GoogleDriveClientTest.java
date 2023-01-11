package com.ogerardin.xplane.util;

import com.google.api.services.drive.model.File;
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
import java.util.Comparator;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

@Slf4j
class GoogleDriveClientTest {

    private static final String FOLDER_ID = "1RHz4PQqWNGGpVG9GaHr84kuGs8LM2xyK";

    private GoogleDriveClient client;

    @BeforeEach
    void setUp() throws GeneralSecurityException, IOException {
        client = new GoogleDriveClient();
    }

    @Test
    @Disabled
    void getFiles() throws GeneralSecurityException, IOException {
        client.getFiles(FOLDER_ID);
    }

    @Test
    void download() throws GeneralSecurityException, IOException {
        File file = client.getFiles(FOLDER_ID).stream().min(Comparator.comparing(File::getSize)).orElseThrow();
        log.debug("Downloading {}, size {}", file, file.getSize());

        Path tempFile = Files.createTempFile("xpman", "tmp");
        log.debug("Downloading to {}", tempFile);
        try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
            client.downloadFile(file.getId(), outputStream);
        }
    }

    @Test
    void download2() throws GeneralSecurityException, IOException {
        File file = client.getFiles(FOLDER_ID).stream().min(Comparator.comparing(File::getSize)).orElseThrow();
        log.debug("Downloading {}", file);

        Path targetFile = Path.of("resumable.part");
        log.debug("Downloading to {}", targetFile);

        URL url = client.getDownloadUrl(file.getId());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (Files.exists(targetFile)) {
            long size = Files.size(targetFile);
            log.debug("Resuming from {}", size);
            connection.setRequestProperty("Range", "bytes=" + size + "-");
        }
        connection.connect();

        try (
                InputStream inputStream = connection.getInputStream();
                OutputStream outputStream = Files.newOutputStream(targetFile, CREATE, APPEND);
        ) {
            IOUtils.copy(inputStream, outputStream);
        }
    }
}