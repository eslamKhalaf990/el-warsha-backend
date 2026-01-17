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
import java.util.ArrayList;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public List<String> saveImagesToTemp(List<MultipartFile> files) throws IOException {
        List<String> tempPaths = new ArrayList<>();

        if (files == null || files.isEmpty()) {
            return tempPaths;
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            // 1. Extract the original extension (e.g., ".jpg")
            String originalName = file.getOriginalFilename();
            String extension = ".tmp"; // Default fallback
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            // 2. Create a temp file with a unique name
            // "order_" is the prefix, extension is the suffix
            Path tempFile = Files.createTempFile("order_upload_", extension);

            // 3. Save the uploaded content to that file
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            // 4. Add absolute path to list
            tempPaths.add(tempFile.toFile().getAbsolutePath());
        }

        return tempPaths;
    }

    // Updated signature to accept a List of Strings
    public void sendNewOrderNotification(String customerName, String orderId, double orderTotal, List<String> imagePaths) {

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

            if (imagePaths != null && !imagePaths.isEmpty()) {
                for (String path : imagePaths) {
                    FileSystemResource file = new FileSystemResource(new File(path));

                    if (file.exists()) {
                        // attach the file using its original name
                        helper.addAttachment(file.getFilename(), file);
                    } else {
                        System.out.println("Warning: Image not found at " + path);
                    }
                }
            }

            javaMailSender.send(message);
            System.out.println("Order notification sent with " + imagePaths.size() + " attachments.");

        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }
}