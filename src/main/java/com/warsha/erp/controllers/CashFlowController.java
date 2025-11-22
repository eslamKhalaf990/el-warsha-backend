package com.warsha.erp.controllers;

import com.google.api.client.util.DateTime;
import com.warsha.erp.dtos.DailyCashFlowDto;
import com.warsha.erp.dtos.RevenueSummaryDto;
import com.warsha.erp.dtos.TopProductDTO;
import com.warsha.erp.services.CashFlowService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController()
@RequestMapping("/cashFlow")
public class CashFlowController {

    private final CashFlowService cashflowService;

    public CashFlowController(CashFlowService cashflowService) {
        this.cashflowService = cashflowService;
    }

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
}
