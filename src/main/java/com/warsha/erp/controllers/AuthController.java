package com.warsha.erp.controllers;

import com.warsha.erp.config.JwtUtil;
import com.warsha.erp.entities.Customer;
import com.warsha.erp.entities.User;
import com.warsha.erp.services.CustomerService;
import com.warsha.erp.services.UserService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private AuthenticationManager authManager;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserService userService;
    @Autowired private CustomerService customerService;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            String token = jwtUtil.generateToken(request.getUsername(), "ADMIN");
            return ResponseEntity.ok(new ERPLogin(token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/customerLogin")
    public ResponseEntity<?> customerLogin(@RequestBody LoginRequest request) {
        Customer customer = customerService.findByEmail(request.getUsername());

        if (customer != null && passwordEncoder.matches(request.getPassword(), customer.getPassword())) {

            String token = jwtUtil.generateToken(customer.getEmail(), "CUSTOMER");
            return ResponseEntity.ok(new CustomerLogin(token, customer.getAddress(), customer.getFullName(), customer.getPhone(), customer.getEmail()));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid customer credentials");
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @Getter
    static class LoginRequest {
        private String username;
        private String password;
    }

    record CustomerLogin(String token, String address, String name, String phone, String email) {
    }

    record ERPLogin(String token) {
    }
}
