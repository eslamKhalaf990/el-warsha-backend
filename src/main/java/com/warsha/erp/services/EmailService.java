package com.warsha.erp.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    // Formatter for consistent log timestamps
    private final DateTimeFormatter logFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private String getTimestamp() {
        return LocalDateTime.now().format(logFormat);
    }

    public List<String> saveImagesToTemp(List<MultipartFile> files) throws IOException {
        System.out.println("[" + getTimestamp() + "] INFO: Processing " + (files != null ? files.size() : 0) + " files for temporary storage");
        List<String> tempPaths = new ArrayList<>();

        if (files == null || files.isEmpty()) {
            return tempPaths;
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                System.out.println("[" + getTimestamp() + "] WARN: Skipping empty file upload");
                continue;
            }

            String originalName = file.getOriginalFilename();
            String extension = ".tmp";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            Path tempFile = Files.createTempFile("order_upload_", extension);
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            String absolutePath = tempFile.toFile().getAbsolutePath();
            System.out.println("[" + getTimestamp() + "] INFO: File saved to temp: " + absolutePath);
            tempPaths.add(absolutePath);
        }

        return tempPaths;
    }

    public void sendNewOrderNotification(String customerName, String orderId, double orderTotal, List<String> imagePaths) {
        System.out.println("[" + getTimestamp() + "] INFO: Preparing email notification for Order ID: " + orderId);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("elwarsha77a@gmail.com");
            helper.setTo("ahmednaser77a@gmail.com");
            helper.setSubject("New Order Received! (ID: " + orderId + ")");

            String emailBody = String.format("""
            Hello Ahmed,
            
            Great news! You have received a new order.
            
            --------------------------------
            Order ID:      %s
            Customer Name: %s
            Total Amount:  EGP %.2f
            --------------------------------
            
            See the attached images for order details.
            """, orderId, customerName, orderTotal);

            helper.setText(emailBody);

            int attachmentCount = 0;
            if (imagePaths != null && !imagePaths.isEmpty()) {
                for (String path : imagePaths) {
                    FileSystemResource file = new FileSystemResource(new File(path));

                    if (file.exists()) {
                        helper.addAttachment(file.getFilename(), file);
                        attachmentCount++;
                    } else {
                        System.out.println("[" + getTimestamp() + "] WARN: Attachment missing at path: " + path);
                    }
                }
            }

            javaMailSender.send(message);
            System.out.println("[" + getTimestamp() + "] SUCCESS: Email sent for Order " + orderId + " with " + attachmentCount + " attachments.");

        } catch (MessagingException e) {
            System.out.println("[" + getTimestamp() + "] ERROR: MessagingException while sending email: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[" + getTimestamp() + "] ERROR: Unexpected error during email process: " + e.getMessage());
        }
    }
}