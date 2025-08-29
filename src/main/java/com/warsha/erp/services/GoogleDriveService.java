package com.warsha.erp.services;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class GoogleDriveService {

    private static final String APPLICATION_NAME = "MyApp";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens"; // Stores access & refresh tokens

    // Path to OAuth2 client_secret.json (downloaded from Google Cloud Console)
    private static final String CREDENTIALS_FILE_PATH = "src/main/resources/client_secret_881242863381-rubnpv9csakd68e5hgt30bll7om5fbde.apps.googleusercontent.com.json";

    /**
     * Creates an authorized Credential object using OAuth2.
     */
    private static com.google.api.client.auth.oauth2.Credential getCredentials() throws IOException, GeneralSecurityException {
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow with offline access to refresh tokens automatically
        List<String> scopes = Collections.singletonList(DriveScopes.DRIVE_FILE);
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        // Triggers browser-based authentication only first time
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Build Drive service using OAuth credentials.
     */
    public static Drive getDriveService() throws Exception {
        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                getCredentials()
        ).setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * Uploads a file to Google Drive (to personal account using OAuth).
     */
    public String uploadFile(MultipartFile multipartFile) throws Exception {
        Drive driveService = getDriveService();
        String folderId = "1PCfi0vdmcArMoXKRjgNL4lPNq3WnnSkL";

        // File metadata
        File fileMetadata = new File();
        fileMetadata.setName(String.valueOf(UUID.randomUUID()));
        fileMetadata.setParents(Collections.singletonList(folderId));

        // File content
        InputStreamContent mediaContent = new InputStreamContent(
                multipartFile.getContentType(),
                new BufferedInputStream(multipartFile.getInputStream())
        );
        mediaContent.setLength(multipartFile.getSize());

        File uploadedFile = driveService.files()
                .create(fileMetadata,
                        new InputStreamContent(multipartFile.getContentType(),
                                multipartFile.getInputStream()))
                .setFields("id")
                .execute();

        // Make file publicly viewable
        driveService.permissions().create(uploadedFile.getId(),
                        new Permission().setType("anyone").setRole("reader"))
                .execute();
        // Return public preview link
        return "https://drive.google.com/uc?export=view&id=" + uploadedFile.getId();
    }
}
