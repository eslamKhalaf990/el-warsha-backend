package com.warsha.erp.config;

import com.warsha.erp.repository.CustomerRepository; // You need this repo
import com.warsha.erp.entities.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomerDetailsService implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Look up by Email specifically
        Customer customer = customerRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with email: " + email));

        // 2. Return the wrapper containing the ID
        return new CustomerUserDetails(customer);
    }
}