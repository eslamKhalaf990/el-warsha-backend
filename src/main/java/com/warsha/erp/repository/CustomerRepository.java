package com.warsha.erp.repository;

import com.warsha.erp.dtos.CustomerCountByGovernorate;
import com.warsha.erp.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("SELECT new com.warsha.erp.dtos.CustomerCountByGovernorate(c.governorate, COUNT(c)) FROM Customer c GROUP BY c.governorate")
    List<CustomerCountByGovernorate> countCustomersByGovernorate();
}
