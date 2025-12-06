package com.warsha.erp.repository;
import com.warsha.erp.entities.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
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

    @Query(value = """
        SELECT TOP 5
            p.ProductID,
            p.Name,
            SUM(oi.Quantity * (oi.UnitPrice - p.BuyingPrice)) AS TotalProfitGenerated,
            SUM(oi.Quantity) AS TotalSoldForMonth
        FROM
            dbo.Orders o
        JOIN
            dbo.OrderItems oi ON o.OrderID = oi.OrderID
        JOIN
            dbo.Products p ON oi.ProductID = p.ProductID
        WHERE
            -- Use the :targetDate parameter passed from the method
            YEAR(o.OrderDate) = YEAR(:targetDate)
            AND
            MONTH(o.OrderDate) = MONTH(:targetDate)
        GROUP BY
            p.ProductID, p.Name
        ORDER BY
            TotalSoldForMonth DESC
        """, nativeQuery = true)
    List<Object[]> getTop5SoldProductsForMonth(@Param("targetDate") Date targetDate);
}
