package com.warsha.erp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendNewOrderNotification(String customerName, String orderId, double orderTotal) {

        // 1. Prepare the email content
        String emailBody = String.format("""
            Hello Ahmed,
            
            Great news! You have received a new order.
            
            --------------------------------
            Order ID:      %s
            Customer Name: %s
            Total Amount:  EGP %.2f
            --------------------------------
            
            Please log in to your dashboard to process this order.
            """, orderId, customerName, orderTotal);

        // 2. Configure the message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("elwarsha77a@gmail.com");
        message.setTo("ahmednaser77a@gmail.com");
        message.setSubject("New Order Received! (ID: " + orderId + ")");
        message.setText(emailBody);

        // 3. Send the email
        javaMailSender.send(message);

        System.out.println("Order notification sent to owner for Order ID: " + orderId);
    }
}