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

    // --- Existing Queries ---

    @Query(value = "SELECT Day, TotalOrders, DailyCashReceived, DailyShippedValue, DailyDeliveryCharges FROM vw_DailyCashFlow", nativeQuery = true)
    List<Object[]> getDailyCashFlowRaw();

    @Query(value = "SELECT ActualCashReceived, ExpectedCash, PotentialRevenue FROM vw_RevenueSummary", nativeQuery = true)
    List<Object[]> getRevenueSummaryRaw();

    @Query(value = """
        SELECT TOP 5 p.ProductID, p.Name, SUM(oi.Quantity * (oi.UnitPrice - p.BuyingPrice)) AS TotalProfitGenerated, SUM(oi.Quantity) AS TotalSoldForMonth
        FROM dbo.Orders o
        JOIN dbo.OrderItems oi ON o.OrderID = oi.OrderID
        JOIN dbo.Products p ON oi.ProductID = p.ProductID
        WHERE YEAR(o.OrderDate) = YEAR(:targetDate) AND MONTH(o.OrderDate) = MONTH(:targetDate)
        GROUP BY p.ProductID, p.Name
        ORDER BY TotalProfitGenerated DESC
        """, nativeQuery = true)
    List<Object[]> getTop5SoldProductsForMonth(@Param("targetDate") Date targetDate);

    // --- New Business Intelligence Queries ---

    /** 1. Repeat Customers (Loyalty) */
    @Query(value = """
        SELECT o.CustomerID, c.FullName, COUNT(o.OrderID) AS OrderCount
        FROM dbo.Orders o
        JOIN dbo.Customers c ON o.CustomerID = c.CustomerID
        GROUP BY o.CustomerID, c.FullName
        HAVING COUNT(o.OrderID) > 1
        """, nativeQuery = true)
    List<Object[]> getRepeatCustomers();

    /** 2. VIP Customers (Highest LTV) */
    @Query(value = """
        SELECT TOP 10 c.FullName, COUNT(o.OrderID) AS TotalOrders, SUM(o.TotalPrice) AS TotalLifetimeSpend, AVG(o.TotalPrice) AS AverageOrderValue
        FROM dbo.Orders o
        JOIN dbo.Customers c ON o.CustomerID = c.CustomerID
        WHERE o.Status != 'Cancelled'
        GROUP BY c.FullName, o.CustomerID
        ORDER BY TotalLifetimeSpend DESC
        """, nativeQuery = true)
    List<Object[]> getTopVIPCustomers();

    /** 3. At-Risk / Lapsed Customers (90+ Days Churn) */
    @Query(value = """
        SELECT c.FullName, c.Phone, COUNT(o.OrderID) AS LifetimeOrders, MAX(o.OrderDate) AS LastOrderDate, DATEDIFF(day, MAX(o.OrderDate), GETDATE()) AS DaysSinceLastOrder
        FROM dbo.Orders o
        JOIN dbo.Customers c ON o.CustomerID = c.CustomerID
        GROUP BY c.FullName, c.Phone, o.CustomerID
        HAVING DATEDIFF(day, MAX(o.OrderDate), GETDATE()) > 90
        ORDER BY DaysSinceLastOrder DESC
        """, nativeQuery = true)
    List<Object[]> getAtRiskCustomers();

    /** 4. Revenue by Order Source (App vs Web vs Phone) */
    @Query(value = """
        SELECT o.OrderSource, COUNT(o.OrderID) AS TotalOrders, SUM(o.TotalPrice) AS TotalRevenue, AVG(o.TotalPrice) AS AverageOrderValue
        FROM dbo.Orders o
        WHERE o.Status != 'Cancelled'
        GROUP BY o.OrderSource
        ORDER BY TotalRevenue DESC
        """, nativeQuery = true)
    List<Object[]> getRevenueBySource();

    /** 5. Discount Seekers */
    @Query(value = """
        SELECT c.FullName, COUNT(o.OrderID) AS TotalOrders, SUM(o.TotalPrice) AS TotalPaid, SUM(o.Discount) AS TotalDiscountsReceived
        FROM dbo.Orders o
        JOIN dbo.Customers c ON o.CustomerID = c.CustomerID
        GROUP BY c.FullName, o.CustomerID
        HAVING SUM(o.Discount) > 0
        ORDER BY TotalDiscountsReceived DESC
        """, nativeQuery = true)
    List<Object[]> getDiscountSeekers();

    /** 6. Best-Selling Products (Top 20) */
    @Query(value = """
        SELECT TOP 20 oi.ProductID, SUM(oi.Quantity) AS TotalUnitsSold, SUM(oi.Quantity * oi.UnitPrice) AS TotalRevenueGenerated
        FROM dbo.OrderItems oi
        JOIN dbo.Orders o ON oi.OrderID = o.OrderID
        WHERE o.Status != 'Cancelled'
        GROUP BY oi.ProductID
        ORDER BY TotalUnitsSold DESC
        """, nativeQuery = true)
    List<Object[]> getTop20Products();

    /** 7. Sales Performance by Governorate */
    @Query(value = """
        SELECT c.Governorate, COUNT(DISTINCT o.OrderID) AS TotalOrders, SUM(o.TotalPrice) AS TotalRevenue, SUM(o.DeliveryCharge) AS TotalDeliveryCollected
        FROM dbo.Orders o
        JOIN dbo.Customers c ON o.CustomerID = c.CustomerID
        WHERE o.Status != 'Cancelled'
        GROUP BY c.Governorate
        ORDER BY TotalRevenue DESC
        """, nativeQuery = true)
    List<Object[]> getPerformanceByGovernorate();

    /** 8. Average Basket Size */
    @Query(value = """
        SELECT AVG(CAST(TotalItems AS FLOAT)) AS AverageItemsPerOrder
        FROM (SELECT OrderID, SUM(Quantity) AS TotalItems FROM dbo.OrderItems GROUP BY OrderID) AS OrderQuantities
        """, nativeQuery = true)
    Double getAverageBasketSize();

    /** 9. Daily Revenue Report (dd mm yyyy) - CLR Safe Version */
    @Query(value = """
        SELECT 
            REPLACE(CONVERT(VARCHAR(10), o.OrderDate, 105), '-', ' ') AS SalesDate, 
            COUNT(o.OrderID) AS NumberOfOrders, 
            SUM(o.Discount) AS TotalDiscountsGiven, 
            SUM(o.TotalPrice) AS NetRevenue
        FROM dbo.Orders o
        WHERE o.Status != 'Cancelled'
        GROUP BY 
            REPLACE(CONVERT(VARCHAR(10), o.OrderDate, 105), '-', ' '), 
            CAST(o.OrderDate AS DATE)
        ORDER BY CAST(o.OrderDate AS DATE) DESC
        """, nativeQuery = true)
    List<Object[]> getDailyRevenueReport();
}