package com.warsha.erp.controllers;

import com.warsha.erp.dtos.DailyCashFlowDto;
import com.warsha.erp.dtos.RevenueSummaryDto;
import com.warsha.erp.services.CashFlowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

}
