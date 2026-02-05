package com.warsha.erp.controllers;

import com.warsha.erp.config.JwtUtil;
import com.warsha.erp.entities.Customer;
import com.warsha.erp.entities.User;
import com.warsha.erp.services.CustomerService;
import com.warsha.erp.services.EmailService;
import com.warsha.erp.services.UserService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private AuthenticationManager authManager;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private EmailService emailService;
    @Autowired private UserService userService;
    @Autowired private CustomerService customerService;
    @Autowired private PasswordEncoder passwordEncoder;

    // Formatter for consistent log timestamps
    private final DateTimeFormatter logFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private String getTimestamp() {
        return LocalDateTime.now().format(logFormat);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("[" + getTimestamp() + "] INFO: ERP Login attempt for: " + request.getUsername());
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            String token = jwtUtil.generateToken(request.getUsername(), "ADMIN");
            System.out.println("[" + getTimestamp() + "] SUCCESS: ERP Admin authenticated: " + request.getUsername());
            return ResponseEntity.ok(new ERPLogin(token));
        } catch (BadCredentialsException e) {
            System.out.println("[" + getTimestamp() + "] WARN: ERP Login failed - Invalid credentials for: " + request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (Exception e) {
            System.out.println("[" + getTimestamp() + "] ERROR: ERP Login unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authentication error");
        }
    }

    @PostMapping("/customerLogin")
    public ResponseEntity<?> customerLogin(@RequestBody LoginRequest request) {
        System.out.println("[" + getTimestamp() + "] INFO: Customer Login attempt for: " + request.getUsername());

        try {
            Customer customer = customerService.findByEmail(request.getUsername());

            if (customer != null && passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
                String token = jwtUtil.generateToken(customer.getEmail(), "CUSTOMER");
                System.out.println("[" + getTimestamp() + "] SUCCESS: Customer authenticated: " + customer.getEmail());

                return ResponseEntity.ok(new CustomerLogin(
                        token,
                        customer.getAddress(),
                        customer.getFullName(),
                        customer.getPhone(),
                        customer.getEmail(),
                        customer.getGovernorate())
                );
            }

            System.out.println("[" + getTimestamp() + "] WARN: Customer Login failed - Wrong password or user null for: " + request.getUsername());
        } catch (Exception e) {
            System.out.println("[" + getTimestamp() + "] ERROR: Customer Login service error: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid customer credentials");
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        System.out.println("[" + getTimestamp() + "] INFO: Registering new ERP User: " + user.getUsername());
        User savedUser = userService.registerUser(user);
        System.out.println("[" + getTimestamp() + "] SUCCESS: ERP User registered with Role: " + savedUser.getRole());
        return savedUser;
    }

    @Getter
    static class LoginRequest {
        private String username;
        private String password;
    }

    record CustomerLogin(String token, String address, String name, String phone, String email, String governorate) {
    }

    record ERPLogin(String token) {
    }
}