package com.warsha.erp.repository;

import com.warsha.erp.dtos.OrderCountByGovernorateDto;
import com.warsha.erp.entities.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT new com.warsha.erp.dtos.OrderCountByGovernorateDto(c.governorate, COUNT(o)) " +
            "FROM Order o JOIN o.customer c " +
            "GROUP BY c.governorate ORDER BY COUNT(c) DESC")
    List<OrderCountByGovernorateDto> countOrdersByGovernorate();

    List<Order> findByOrderDateBetween(LocalDate start, LocalDate end, Sort sort);
}
