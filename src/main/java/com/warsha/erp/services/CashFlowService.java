package com.warsha.erp.services;

import com.warsha.erp.dtos.*;
import com.warsha.erp.repository.CashFlowRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CashFlowService {

    private final CashFlowRepository cashFlowRepository;

    public CashFlowService(CashFlowRepository cashFlowRepository) {
        this.cashFlowRepository = cashFlowRepository;
    }

    // --- Existing Methods ---

    public List<DailyCashFlowDto> getDailyCashFlow() {
        return cashFlowRepository.getDailyCashFlowRaw().stream().map(row -> {
            DailyCashFlowDto dto = new DailyCashFlowDto();
            dto.setDay(((java.sql.Date) row[0]).toLocalDate());
            dto.setTotalOrders(((Number) row[1]).longValue());
            dto.setDailyCashReceived((BigDecimal) row[2]);
            dto.setDailyShippedValue((BigDecimal) row[3]);
            dto.setDailyDeliveryCharges((BigDecimal) row[4]);
            return dto;
        }).toList();
    }

    public RevenueSummaryDto getRevenueSummary() {
        return cashFlowRepository.getRevenueSummaryRaw().stream().map(row -> {
            RevenueSummaryDto dto = new RevenueSummaryDto();
            dto.setActualCashReceived((BigDecimal) row[0]);
            dto.setExpectedCash((BigDecimal) row[1]);
            dto.setPotentialRevenue((BigDecimal) row[2]);
            return dto;
        }).toList().getFirst();
    }

    public List<TopProductDTO> getTop5SoldProductsForMonth(Date targetDate) {
        List<Object[]> rawResults = cashFlowRepository.getTop5SoldProductsForMonth(targetDate);
        List<TopProductDTO> dtoList = new ArrayList<>();
        for (Object[] row : rawResults) {
            TopProductDTO dto = new TopProductDTO();
            dto.setProductId(row[0] != null ? ((Number) row[0]).longValue() : null);
            dto.setName((String) row[1]);
            dto.setTotalSold(row[3] != null ? ((Number) row[3]).intValue() : null);
            dto.setTotalProfit(row[2] != null ? ((Number) row[2]).intValue() : null);
            dtoList.add(dto);
        }
        return dtoList;
    }

    // --- New BI Methods ---

    /** 1. Repeat Customers */
    public List<CustomerLoyaltyDto> getRepeatCustomers() {
        return cashFlowRepository.getRepeatCustomers().stream().map(row ->
                new CustomerLoyaltyDto(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue()
                )
        ).toList();
    }

    /** 2. VIP Customers */
    public List<VipCustomerDto> getTopVIPCustomers() {
        return cashFlowRepository.getTopVIPCustomers().stream().map(row ->
                new VipCustomerDto(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        row[2] != null ? ((Number) row[2]).doubleValue() : 0.0,
                        row[3] != null ? ((Number) row[3]).doubleValue() : 0.0
                )
        ).toList();
    }

    /** 3. At-Risk Customers */
    public List<AtRiskCustomerDto> getAtRiskCustomers() {
        return cashFlowRepository.getAtRiskCustomers().stream().map(row ->
                new AtRiskCustomerDto(
                        (String) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        (java.util.Date) row[3],
                        ((Number) row[4]).intValue()
                )
        ).toList();
    }

    /** 4. Revenue by Source */
    public List<OrderSourceDto> getRevenueBySource() {
        return cashFlowRepository.getRevenueBySource().stream().map(row ->
                new OrderSourceDto(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        row[2] != null ? ((Number) row[2]).doubleValue() : 0.0,
                        row[3] != null ? ((Number) row[3]).doubleValue() : 0.0
                )
        ).toList();
    }

    /** 5. Discount Seekers */
    public List<DiscountSeekerDto> getDiscountSeekers() {
        return cashFlowRepository.getDiscountSeekers().stream().map(row ->
                new DiscountSeekerDto(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        row[2] != null ? ((Number) row[2]).doubleValue() : 0.0,
                        row[3] != null ? ((Number) row[3]).doubleValue() : 0.0
                )
        ).toList();
    }

    /** 6. Top 20 Best Selling Products */
    public List<ProductPerformanceDto> getTop20Products() {
        return cashFlowRepository.getTop20Products().stream().map(row ->
                new ProductPerformanceDto(
                        ((Number) row[0]).longValue(),
                        ((Number) row[1]).longValue(),
                        row[2] != null ? ((Number) row[2]).doubleValue() : 0.0
                )
        ).toList();
    }

    /** 7. Governorate Performance */
    public List<RegionalPerformanceDto> getPerformanceByGovernorate() {
        return cashFlowRepository.getPerformanceByGovernorate().stream().map(row ->
                new RegionalPerformanceDto(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        row[2] != null ? ((Number) row[2]).doubleValue() : 0.0,
                        row[3] != null ? ((Number) row[3]).doubleValue() : 0.0
                )
        ).toList();
    }

    /** 8. KPI: Average Basket Size */
    public Double getAverageBasketSize() {
        Double value = cashFlowRepository.getAverageBasketSize();
        return value != null ? value : 0.0;
    }

    /** 9. Daily Revenue Report */
    public List<DailyRevenueReportDto> getDailyRevenueReport() {
        return cashFlowRepository.getDailyRevenueReport().stream().map(row ->
                new DailyRevenueReportDto(
                        (String) row[0], // Already formatted as dd MM yyyy from SQL
                        ((Number) row[1]).longValue(),
                        row[2] != null ? ((Number) row[2]).doubleValue() : 0.0,
                        row[3] != null ? ((Number) row[3]).doubleValue() : 0.0
                )
        ).toList();
    }
}