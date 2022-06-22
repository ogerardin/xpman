package com.ogerardin.xplane.util;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveRequestInitializer;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class GoogleDriveClient {
    private static final String APPLICATION_NAME = "X-Plane Manager";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public static final String API_KEY = "AIzaSyB4Rez-VcN9j9DQAq3cu3PU-Ef3wyz-hq8";

    public List<File> getFiles(String folderId) throws GeneralSecurityException, IOException {
        // Build a new API client service.
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Drive drive = new Drive.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .setGoogleClientRequestInitializer(new DriveRequestInitializer(API_KEY))
                .build();

        // Print the names and IDs for up to 10 files.
        FileList result = drive.files().list()
//                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .setQ(String.format("'%s' in parents", folderId))
//                .setQ("'root' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
//                .setSpaces("drive")
//                .setFields("nextPageToken, files(id, name, parents)")
                .execute();

        return result.getFiles();
    }
}