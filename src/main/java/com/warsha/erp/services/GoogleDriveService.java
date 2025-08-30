package com.warsha.erp.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.util.Collections;
import java.util.UUID;

@Service
public class GoogleDriveService {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.refresh.token}")
    private  String refreshToken;

    @Value("${google.application.name:WarshaBackend}")
    private String applicationName;

    @Value("${google.drive.folder.id}")
    private String folderId;

    private Credential getCredentials() throws Exception {

        if (clientId == null || clientSecret == null || refreshToken == null) {
            throw new IllegalStateException("Google OAuth properties are not set!");
        }

        return new GoogleCredential.Builder()
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setRefreshToken(refreshToken);
    }

    public Drive getDriveService() throws Exception {
        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                getCredentials()
        ).setApplicationName(applicationName).build();
    }

    public String uploadFile(MultipartFile multipartFile) throws Exception {
        Drive driveService = getDriveService();

        // Metadata
        File fileMetadata = new File();
        fileMetadata.setName(UUID.randomUUID().toString());
        fileMetadata.setParents(Collections.singletonList(folderId));

        // Content
        InputStreamContent mediaContent = new InputStreamContent(
                multipartFile.getContentType(),
                new BufferedInputStream(multipartFile.getInputStream())
        );
        mediaContent.setLength(multipartFile.getSize());

        // Upload
        File uploadedFile = driveService.files()
                .create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        // Make it public
        driveService.permissions().create(uploadedFile.getId(),
                        new Permission().setType("anyone").setRole("reader"))
                .execute();

        return "https://drive.google.com/uc?export=view&id=" + uploadedFile.getId();
    }
}
