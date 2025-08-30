package com.warsha.erp.controllers;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.warsha.erp.services.GoogleDriveService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final GoogleDriveService googleDriveService;

    public FileController(GoogleDriveService googleDriveService) {
        this.googleDriveService = googleDriveService;
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> getFile(@PathVariable String fileId) throws Exception {
        Drive driveService = googleDriveService.getDriveService();

        File file = driveService.files().get(fileId)
                .setFields("mimeType, name")
                .execute();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.getMimeType()));
        headers.set("Access-Control-Allow-Origin", "*");
        headers.setContentDispositionFormData("inline", file.getName());

        return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
    }
}
