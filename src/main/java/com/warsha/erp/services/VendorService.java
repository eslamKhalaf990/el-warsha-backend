package com.warsha.erp.services;

import com.warsha.erp.entities.Vendor;
import com.warsha.erp.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorService {
    private final VendorRepository repository;

    public List<Vendor> getAllVendors() {
        return repository.findAll();
    }

    public Vendor saveVendor(Vendor vendor) {
        return repository.save(vendor);
    }

    public Vendor updateVendor(Long id, Vendor details) {
        Vendor vendor = repository.findById(id).orElseThrow();
        vendor.setName(details.getName());
        vendor.setPhone(details.getPhone());
        vendor.setEmail(details.getEmail());
        vendor.setAddress(details.getAddress());
        vendor.setTaxNumber(details.getTaxNumber());
        vendor.setContactPerson(details.getContactPerson());
        vendor.setNotes(details.getNotes());
        return repository.save(vendor);
    }

    public void deleteVendor(Long id) {
        repository.deleteById(id);
    }
}