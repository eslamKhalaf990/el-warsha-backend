package com.warsha.erp.controllers;

import com.warsha.erp.dtos.ValidateVoucherRequest;
import com.warsha.erp.dtos.VoucherValidationResponse;
import com.warsha.erp.services.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vouchers")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @PostMapping("/validate")
    public ResponseEntity<VoucherValidationResponse> validateVoucher(@RequestBody ValidateVoucherRequest request) {
        VoucherValidationResponse response = voucherService.validateVoucher(request);
        return ResponseEntity.ok(response);
    }
}