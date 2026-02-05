package com.warsha.erp.services;

import com.warsha.erp.dtos.CustomerCountByGovernorate;
import com.warsha.erp.entities.Customer;
import com.warsha.erp.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CustomerService {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private EmailService emailService;
    @Autowired private PasswordEncoder passwordEncoder;

    // Formatter for consistent log timestamps
    private final DateTimeFormatter logFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private String getTimestamp() {
        return LocalDateTime.now().format(logFormat);
    }

    public List<Customer> getAllCustomers() {
        System.out.println("[" + getTimestamp() + "] INFO: Fetching all customers (Sorted DESC)");
        return customerRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public List<CustomerCountByGovernorate> getCustomerCountsByGovernorate() {
        System.out.println("[" + getTimestamp() + "] INFO: Fetching customer counts by governorate");
        return customerRepository.countCustomersByGovernorate();
    }

    public Customer findByEmail(String email) {
        System.out.println("[" + getTimestamp() + "] INFO: Finding customer by email: " + email);
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("[" + getTimestamp() + "] ERROR: Customer not found for email: " + email);
                    return new RuntimeException("Customer not found");
                });
    }

    public Customer getCustomerByID(Long id) {
        System.out.println("[" + getTimestamp() + "] INFO: Finding customer by ID: " + id);
        return customerRepository.findById(id)
                .orElseThrow(() -> {
                    System.out.println("[" + getTimestamp() + "] ERROR: Customer not found for ID: " + id);
                    return new RuntimeException("Customer not found");
                });
    }

    public Customer createCustomer(Customer customer) {
        System.out.println("[" + getTimestamp() + "] INFO: Creating new customer: " + customer.getFullName());

        customer.setCreatedAt(LocalDateTime.now());

        if (customer.getPassword() != null) {
            customer.setPassword(passwordEncoder.encode(customer.getPassword()));
            if (!EgyptGovernorates.isValid(customer.getGovernorate())) {
                System.out.println("[" + getTimestamp() + "] WARN: Invalid governorate: " + customer.getGovernorate());
                throw new IllegalArgumentException("Invalid governorate provided: " + customer.getGovernorate());
            }
        }

        Customer saved = customerRepository.save(customer);
        System.out.println("[" + getTimestamp() + "] SUCCESS: Customer saved with ID: " + saved.getId());
        return saved;
    }

    public Customer updateCustomer(Long id, Customer updateCustomer) {
        System.out.println("[" + getTimestamp() + "] INFO: Updating customer ID: " + id);

        Customer existing = getCustomerByID(id);
        existing.setFullName(updateCustomer.getFullName());
        existing.setGovernorate(updateCustomer.getGovernorate());
        existing.setPhone(updateCustomer.getPhone());
        existing.setAddress(updateCustomer.getAddress());
        existing.setUpdatedAt(LocalDateTime.now());

        Customer saved = customerRepository.save(existing);
        System.out.println("[" + getTimestamp() + "] SUCCESS: Customer ID " + id + " updated.");
        return saved;
    }

    public void deleteCustomer(Long id) {
        System.out.println("[" + getTimestamp() + "] WARN: Deleting customer ID: " + id);
        customerRepository.deleteById(id);
    }

    public void deleteAll() {
        System.out.println("[" + getTimestamp() + "] ALERT: Deleting all customer records!");
        customerRepository.deleteAll();
    }
}