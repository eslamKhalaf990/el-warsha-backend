package com.warsha.erp.services;

import com.warsha.erp.dtos.DailyCashFlowDto;
import com.warsha.erp.dtos.RevenueSummaryDto;
import com.warsha.erp.dtos.TopProductDTO;
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

        // 1. Get the raw data from the repository
        List<Object[]> rawResults = cashFlowRepository.getTop5SoldProductsForMonth(targetDate);

        // 2. Manually map the raw data to your new DTO
        List<TopProductDTO> dtoList = new ArrayList<>();
        for (Object[] row : rawResults) {
            TopProductDTO dto = new TopProductDTO();

            // Safely cast and set each field
            dto.setProductId(row[0] != null ? ((Number) row[0]).longValue() : null);
            dto.setName((String) row[1]);
            dto.setTotalSold(row[3] != null ? ((Number) row[3]).intValue() : null);
            dto.setTotalProfit(row[2] != null ? ((Number) row[2]).intValue() : null);

            dtoList.add(dto);
        }

        return dtoList;
    }
}
