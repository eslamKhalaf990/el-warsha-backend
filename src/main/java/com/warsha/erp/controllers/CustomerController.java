package com.warsha.erp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warsha.erp.models.CustomerModel;
import com.warsha.erp.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@CrossOrigin // optional: allows cross-origin requests (good for Flutter)
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public List<CustomerModel> getAll() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerModel> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerByID(id));
    }

    @PostMapping
    public ResponseEntity<CustomerModel> create(@RequestBody CustomerModel customer) {
        return new ResponseEntity<>(customerService.createCustomer(customer), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerModel> update(@PathVariable Long id, @RequestBody CustomerModel product) {
        return ResponseEntity.ok(customerService.updateCustomer(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        customerService.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
