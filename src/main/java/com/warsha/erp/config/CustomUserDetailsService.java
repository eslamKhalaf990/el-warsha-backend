package com.warsha.erp.config;

import com.warsha.erp.repository.UserRepository;
import com.warsha.erp.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    // Formatter for consistent log timestamps
    private final DateTimeFormatter logFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private String getTimestamp() {
        return LocalDateTime.now().format(logFormat);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("[" + getTimestamp() + "] INFO: Attempting login for username: " + username);

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("[" + getTimestamp() + "] ERROR: Login failed - Username '" + username + "' not found in database.");
                    return new UsernameNotFoundException("User not found");
                });

        System.out.println("[" + getTimestamp() + "] SUCCESS: User found. Role: " + user.getRole());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword()) // must be encoded
                .roles(user.getRole()) // e.g., "ADMIN" or "USER"
                .build();
    }
}