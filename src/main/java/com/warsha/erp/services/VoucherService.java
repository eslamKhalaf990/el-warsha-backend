package com.warsha.erp.services;

import com.warsha.erp.dtos.ValidateVoucherRequest;
import com.warsha.erp.dtos.VoucherValidationResponse;
import com.warsha.erp.entities.DiscountType;
import com.warsha.erp.entities.Voucher;
import com.warsha.erp.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    public VoucherValidationResponse validateVoucher(ValidateVoucherRequest request) {
        // 1. Input Sanity Check
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return new VoucherValidationResponse(false, "Code cannot be empty", BigDecimal.ZERO, null);
        }

        // 2. Fetch from DB
        Optional<Voucher> voucherOpt = voucherRepository.findByCode(request.getCode());
        
        if (voucherOpt.isEmpty()) {
            return new VoucherValidationResponse(false, "Invalid Coupon Code", BigDecimal.ZERO, request.getCode());
        }

        Voucher voucher = voucherOpt.get();

        // 3. Run Validations
        if (!voucher.isActive()) {
            return new VoucherValidationResponse(false, "This coupon is inactive", BigDecimal.ZERO, request.getCode());
        }

        LocalDate today = LocalDate.now();
        if (today.isBefore(voucher.getStartDate())) {
             return new VoucherValidationResponse(false, "Promotion has not started yet", BigDecimal.ZERO, request.getCode());
        }
        
        if (voucher.getEndDate() != null && today.isAfter(voucher.getEndDate())) {
            return new VoucherValidationResponse(false, "Coupon has expired", BigDecimal.ZERO, request.getCode());
        }

        if (voucher.getMaxUsageLimit() != null && voucher.getCurrentUsageCount() >= voucher.getMaxUsageLimit()) {
            return new VoucherValidationResponse(false, "Coupon usage limit reached", BigDecimal.ZERO, request.getCode());
        }

        if (request.getCartTotal().compareTo(voucher.getMinOrderAmount()) < 0) {
            return new VoucherValidationResponse(false, 
                "Minimum order of " + voucher.getMinOrderAmount() + " required", 
                BigDecimal.ZERO, 
                request.getCode());
        }

        // 4. Calculate Potential Discount (Simulation)
        BigDecimal discountAmount;

        if (voucher.getType() == DiscountType.PERCENTAGE) {
            BigDecimal percentage = voucher.getDiscountValue().divide(BigDecimal.valueOf(100));
            discountAmount = request.getCartTotal().multiply(percentage);

            // Apply Cap
            if (voucher.getMaxDiscountAmount() != null && discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                discountAmount = voucher.getMaxDiscountAmount();
            }
        } else {
            discountAmount = voucher.getDiscountValue();
        }

        // Safety: Discount can't exceed total
        if (discountAmount.compareTo(request.getCartTotal()) > 0) {
            discountAmount = request.getCartTotal();
        }

        // 5. Return Success
        return new VoucherValidationResponse(true, "Coupon Applied Successfully", discountAmount, voucher.getCode());
    }
}