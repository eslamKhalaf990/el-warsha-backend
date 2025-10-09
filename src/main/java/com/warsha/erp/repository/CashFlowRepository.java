package com.warsha.erp.repository;
import com.warsha.erp.entities.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashFlowRepository extends CrudRepository<Order, Long> {

    @Query(value = """
        SELECT Day,
            TotalOrders,
            DailyCashReceived,
            DailyShippedValue,
            DailyDeliveryCharges
        FROM vw_DailyCashFlow
        """, nativeQuery = true)
    List<Object[]> getDailyCashFlowRaw();

    @Query(value = """
        SELECT ActualCashReceived,
            ExpectedCash,
            PotentialRevenue
        FROM vw_RevenueSummary
        """, nativeQuery = true)
    List<Object[]> getRevenueSummaryRaw();
}
