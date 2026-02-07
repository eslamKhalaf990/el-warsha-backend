package com.warsha.erp.repository;

import com.warsha.erp.entities.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    List<Vendor> findByIsActiveTrue(); // To get only active suppliers
}