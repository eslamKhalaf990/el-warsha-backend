package com.warsha.erp.config;

import com.warsha.erp.repository.CustomerRepository;
import com.warsha.erp.entities.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CustomerDetailsService implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepo;

    // Formatter for consistent log timestamps
    private final DateTimeFormatter logFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private String getTimestamp() {
        return LocalDateTime.now().format(logFormat);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("[" + getTimestamp() + "] INFO: Customer login attempt with email: " + email);

        // 1. Look up by Email specifically
        Customer customer = customerRepo.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("[" + getTimestamp() + "] ERROR: Authentication failed - Customer email '" + email + "' not found.");
                    return new UsernameNotFoundException("Customer not found with email: " + email);
                });

        System.out.println("[" + getTimestamp() + "] SUCCESS: Customer record found for " + email + " (ID: " + customer.getId() + ")");

        // 2. Return the wrapper containing the ID
        return new CustomerUserDetails(customer);
    }
}