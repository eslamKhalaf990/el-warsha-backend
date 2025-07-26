package com.warsha.erp.services;

import com.warsha.erp.entities.Customer;
import com.warsha.erp.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerByID(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public Customer createCustomer(Customer customer) {
        customer.setCreatedAt(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer updateCustomer) {
        Customer existing = getCustomerByID(id);
        existing.setFullName(updateCustomer.getFullName());
        existing.setEmail(updateCustomer.getEmail());
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
