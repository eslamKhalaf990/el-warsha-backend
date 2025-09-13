package com.warsha.erp.repository;

import com.warsha.erp.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.deleted IS NULL OR p.deleted <> 'true' ORDER BY p.id DESC")
    List<Product> findAllNotDeleted();
}
