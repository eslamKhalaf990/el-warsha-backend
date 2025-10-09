package com.warsha.erp.services;

import com.warsha.erp.dtos.DailyCashFlowDto;
import com.warsha.erp.dtos.RevenueSummaryDto;
import com.warsha.erp.repository.CashFlowRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
}
