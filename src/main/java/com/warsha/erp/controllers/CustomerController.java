package com.warsha.erp.controllers;

import com.warsha.erp.config.JwtUtil;
import com.warsha.erp.dtos.CustomerCountByGovernorate;
import com.warsha.erp.entities.Customer;
import com.warsha.erp.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired private JwtUtil jwtUtil;

    @Autowired private PasswordEncoder passwordEncoder;

    // Formatter for consistent log timestamps
    private final DateTimeFormatter logFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private String getTimestamp() {
        return LocalDateTime.now().format(logFormat);
    }

    @GetMapping
    public List<Customer> getAll() {
        System.out.println("[" + getTimestamp() + "] INFO: Request to fetch all customers");
        return customerService.getAllCustomers();
    }

    @GetMapping("/countsByGovernorate")
    public List<CustomerCountByGovernorate> getCountsByGovernorate() {
        System.out.println("[" + getTimestamp() + "] INFO: Request for customer counts by governorate");
        return customerService.getCustomerCountsByGovernorate();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Long id) {
        System.out.println("[" + getTimestamp() + "] INFO: Request to fetch customer ID: " + id);
        return ResponseEntity.ok(customerService.getCustomerByID(id));
    }

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody Customer customer) {
        System.out.println("[" + getTimestamp() + "] INFO: Request to create new customer (ERP Admin): " + customer.getFullName());
        return new ResponseEntity<>(customerService.createCustomer(customer), HttpStatus.CREATED);
    }

    @PostMapping("/customerSignUp")
    public ResponseEntity<?> customerSignUp(@RequestBody Customer customer) {
        System.out.println("[" + getTimestamp() + "] INFO: New customer self-signup attempt: " + customer.getEmail());

        try {
            customerService.createCustomer(customer);
            String token = jwtUtil.generateToken(customer.getEmail(), "CUSTOMER");

            System.out.println("[" + getTimestamp() + "] SUCCESS: Customer sign-up successful for " + customer.getEmail());

            return ResponseEntity.ok(new AuthController.CustomerLogin(
                    token,
                    customer.getAddress(),
                    customer.getFullName(),
                    customer.getPhone(),
                    customer.getEmail(),
                    customer.getGovernorate()
            ));
        } catch (Exception e) {
            System.out.println("[" + getTimestamp() + "] ERROR: Customer sign-up failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(@PathVariable Long id, @RequestBody Customer customer) {
        System.out.println("[" + getTimestamp() + "] INFO: Request to update customer ID: " + id);
        return ResponseEntity.ok(customerService.updateCustomer(id, customer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        System.out.println("[" + getTimestamp() + "] WARN: Request to delete customer ID: " + id);
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        System.out.println("[" + getTimestamp() + "] ALERT: Request to DELETE ALL customers received!");
        customerService.deleteAll();
        return ResponseEntity.noContent().build();
    }
}