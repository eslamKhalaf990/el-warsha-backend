package com.warsha.erp.config;

import com.warsha.erp.entities.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;

public class CustomerUserDetails implements UserDetails {

    private final Customer customer;

    // Formatter for consistent log timestamps
    private final DateTimeFormatter logFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public CustomerUserDetails(Customer customer) {
        this.customer = customer;
        System.out.println("[" + getTimestamp() + "] INFO: Mapping Security Principal for Customer: " + customer.getEmail());
    }

    private String getTimestamp() {
        return LocalDateTime.now().format(logFormat);
    }

    public Long getId() {
        return customer.getId();
    }

    @Override
    public String getUsername() {
        return customer.getEmail();
    }

    @Override
    public String getPassword() {
        return customer.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Log authority check for debugging access issues
        System.out.println("[" + getTimestamp() + "] DEBUG: Authority check for " + customer.getEmail() + " -> ROLE_CUSTOMER");
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}