package com.ogerardin.xplane.util;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveRequestInitializer;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
public class GoogleDriveClient {
    private static final String APPLICATION_NAME = "X-Plane Manager";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public static final String API_KEY = "AIzaSyB4Rez-VcN9j9DQAq3cu3PU-Ef3wyz-hq8";

    private final Drive drive;

    public GoogleDriveClient() throws GeneralSecurityException, IOException {
        drive = getDrive();
    }

    public List<File> getFiles(String folderId) throws GeneralSecurityException, IOException {
        Drive drive = getDrive();

        // Print the names and IDs for up to 10 files.
        FileList result = drive.files().list()
//                .setPageSize(10)
                .setFields("nextPageToken, files(id, name, size, mimeType)")
                .setQ(String.format("'%s' in parents", folderId))
//                .setQ("'root' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
//                .setSpaces("drive")
//                .setFields("nextPageToken, files(id, name, parents)")
                .execute();

        List<File> files = result.getFiles();
        log.debug("Listed {} files in folder {}", files.size(), folderId);
        files.forEach(f -> log.debug("  {} ({}, id={}, mimeType={})", f.getName(), f.getSize(), f.getId(), f.getMimeType()));
        return files;
    }

    /**
     */
    public void downloadFile(String realFileId, OutputStream outputStream) throws IOException {
        drive.files().get(realFileId).executeMediaAndDownloadTo(outputStream);
    }

    private Drive getDrive() throws GeneralSecurityException, IOException {
        // Build a new API client service.
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Drive drive = new Drive.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .setGoogleClientRequestInitializer(new DriveRequestInitializer(API_KEY))
                .build();
        return drive;
    }

    public URL getDownloadUrl(String realFileId) throws IOException {
        GenericUrl genericUrl = drive.files().get(realFileId).set("alt", "media").
                buildHttpRequestUrl();
        // buildHttpRequestUrl() does not run the DriveRequestInitializer, so the API key
        // would be missing from the URL and a raw HttpURLConnection would 403 — add it explicitly.
        genericUrl.set("key", API_KEY);
        return genericUrl.toURL();

    }
}