package com.warsha.erp.services;

import com.warsha.erp.dtos.CustomerCountByGovernorate;
import com.warsha.erp.entities.Customer;
import com.warsha.erp.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerService {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private EmailService emailService;
    @Autowired private PasswordEncoder passwordEncoder;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public List<CustomerCountByGovernorate> getCustomerCountsByGovernorate() {
        return customerRepository.countCustomersByGovernorate();
    }

    public Customer findByEmail(String email) {
        System.out.println(email);
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public Customer getCustomerByID(Long id) {
        System.out.println(id);
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public Customer createCustomer(Customer customer) {

        // 2. Set timestamps
        customer.setCreatedAt(LocalDateTime.now());

        // 3. Encode Password
        if (customer.getPassword() != null) {
            customer.setPassword(passwordEncoder.encode(customer.getPassword()));
            if (!EgyptGovernorates.isValid(customer.getGovernorate())) {
                throw new IllegalArgumentException("Invalid governorate provided: " + customer.getGovernorate());
            }
        }
        System.out.println(customer);
        // 4. Save
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer updateCustomer) {
        Customer existing = getCustomerByID(id);
        existing.setFullName(updateCustomer.getFullName());
        existing.setGovernorate(updateCustomer.getGovernorate());
        existing.setPhone(updateCustomer.getPhone());
        existing.setAddress(updateCustomer.getAddress());
        existing.setUpdatedAt(LocalDateTime.now());
        return customerRepository.save(existing);
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    public void deleteAll() {
        customerRepository.deleteAll();
    }
}

