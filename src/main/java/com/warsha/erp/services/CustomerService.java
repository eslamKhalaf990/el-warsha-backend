package com.warsha.erp.services;

import com.warsha.erp.models.CustomerModel;
import com.warsha.erp.models.ProductModel;
import com.warsha.erp.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<CustomerModel> getAllCustomers() {
        return customerRepository.findAll();
    }

    public CustomerModel getCustomerByID(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public CustomerModel createCustomer(CustomerModel customer) {
        customer.setCreatedAt(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    public CustomerModel updateCustomer(Long id, CustomerModel updateCustomer) {
        CustomerModel existing = getCustomerByID(id);
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
