package com.warsha.erp.controllers;

import com.warsha.erp.dtos.InvoiceDto;
import com.warsha.erp.entities.Invoice;
import com.warsha.erp.services.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/invoice")
@CrossOrigin
public class InvoiceController {
    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/{id}")
    public ResponseEntity<InvoiceDto> generateInvoice(@PathVariable Long id) {
        Invoice invoice = invoiceService.generateInvoice(id);
        InvoiceDto invoiceDto = new InvoiceDto(invoice);

        return ResponseEntity.ok(invoiceDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        InvoiceDto invoiceDto = new InvoiceDto(invoice);
        return ResponseEntity.ok(invoiceDto);
    }

    @GetMapping
    public ResponseEntity<List<InvoiceDto>> getAllInvoices() {
        List<Invoice> invoices = invoiceService.getAllInvoices();

        List<InvoiceDto> invoiceDTOs = invoices.stream()
                .map(InvoiceDto::new)
                .toList();

        return ResponseEntity.ok(invoiceDTOs);
    }

    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        byte[] pdfBytes = invoiceService.generateInvoicePdf(invoice);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=invoice-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

}
