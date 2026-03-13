package com.warsha.erp.controllers;

import com.warsha.erp.dtos.*;
import com.warsha.erp.services.CashFlowService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/cashFlow")
public class CashFlowController {

    private final CashFlowService cashflowService;

    public CashFlowController(CashFlowService cashflowService) {
        this.cashflowService = cashflowService;
    }

    // --- Existing Endpoints ---

    @GetMapping("/daily")
    public List<DailyCashFlowDto> getDailyCashFlow() {
        return cashflowService.getDailyCashFlow();
    }

    @GetMapping("/revenueSummary")
    public RevenueSummaryDto getRevenueSummary() {
        return cashflowService.getRevenueSummary();
    }

    @GetMapping("/topSoldProducts")
    public List<TopProductDTO> getTop5SoldProductsForMonth(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date targetDate
    ) {
        return cashflowService.getTop5SoldProductsForMonth(targetDate);
    }

    // --- New BI Analysis Endpoints ---

    /** 1 & 2. Customer Insights (Repeat & VIP) */
    @GetMapping("/analysis/customers/loyalty")
    public List<CustomerLoyaltyDto> getCustomerLoyaltyReport() {
        return cashflowService.getRepeatCustomers();
    }

    @GetMapping("/analysis/customers/vip")
    public List<VipCustomerDto> getTopVipCustomers() {
        return cashflowService.getTopVIPCustomers();
    }

    /** 3. Churn Analysis */
    @GetMapping("/analysis/customers/at-risk")
    public List<AtRiskCustomerDto> getAtRiskCustomers() {
        return cashflowService.getAtRiskCustomers();
    }

    /** 4. Sales Channel Performance */
    @GetMapping("/analysis/revenue-by-source")
    public List<OrderSourceDto> getRevenueBySource() {
        return cashflowService.getRevenueBySource();
    }

    /** 5. Marketing/Discount Analysis */
    @GetMapping("/analysis/discount-seekers")
    public List<DiscountSeekerDto> getDiscountSeekers() {
        return cashflowService.getDiscountSeekers();
    }

    /** 6. Product Performance (Top 20) */
    @GetMapping("/analysis/products/top-performers")
    public List<ProductPerformanceDto> getTop20Products() {
        return cashflowService.getTop20Products();
    }

    /** 7. Regional Market Analysis */
    @GetMapping("/analysis/governorate-performance")
    public List<RegionalPerformanceDto> getPerformanceByGovernorate() {
        return cashflowService.getPerformanceByGovernorate();
    }

    /** 8. KPI: Average Basket Size */
    @GetMapping("/analysis/kpi/average-basket-size")
    public Double getAverageBasketSize() {
        return cashflowService.getAverageBasketSize();
    }

    /** 9. Full Daily Revenue History */
    @GetMapping("/analysis/daily-revenue-report")
    public List<DailyRevenueReportDto> getDailyRevenueReport() {
        return cashflowService.getDailyRevenueReport();
    }
}