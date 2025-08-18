package com.warsha.erp.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.warsha.erp.entities.Invoice;
import com.warsha.erp.entities.Order;
import com.warsha.erp.repository.InvoiceRepository;
import com.warsha.erp.repository.OrderRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

@Service
public class InvoiceService {
    final InvoiceRepository invoiceRepository;
    final OrderRepository orderRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          OrderRepository orderRepository) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
    }

    public Invoice generateInvoice(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getInvoice() != null) {
            throw new RuntimeException("Invoice already exists for this order");
        }

        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());

        order.setInvoice(invoice); // keep both sides in sync

        return invoiceRepository.save(invoice);
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public byte[] generateInvoicePdf(Invoice invoice) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);

            document.open();

            // Create a table with 2 columns (left: text, right: logo)
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{70, 30});

            // Left Cell: Invoice Info
            PdfPCell textCell = new PdfPCell();
            textCell.setBorder(Rectangle.NO_BORDER);
            textCell.addElement(new Paragraph("Invoice #" + invoice.getId(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            textCell.addElement(new Paragraph("Date: " + invoice.getIssuedDate().toString()));
            textCell.addElement(new Paragraph("Customer: " + invoice.getOrder().getCustomer().getFullName()));
            textCell.addElement(new Paragraph("Phone: " + invoice.getOrder().getCustomer().getPhone()));
            textCell.addElement(new Paragraph("Address: " + invoice.getOrder().getCustomer().getAddress()));

            // Right Cell: Logo
            InputStream logoStream = new ClassPathResource("static/logo.jpg").getInputStream();

            // Create Image from InputStream
            Image logo = Image.getInstance(logoStream.readAllBytes());

            logo.scaleToFit(100, 100);

            PdfPCell logoCell = new PdfPCell(logo);
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            // Add cells to header table
            headerTable.addCell(textCell);
            headerTable.addCell(logoCell);

            // Add the header to the document
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            headerTable.setSpacingAfter(20f); // adds 20pt space after the table
            document.add(headerTable);


            // Table for items
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(80);

            // Header row with padding
            Stream.of("Product", "Quantity", "Price", "Total")
                    .forEach(columnTitle -> {
                        PdfPCell header = new PdfPCell(new Phrase(columnTitle));
                        header.setPadding(10f); // padding inside header cell
                        table.addCell(header);
                    });

            // Data rows with padding
            invoice.getOrder().getItems().forEach(item -> {
                PdfPCell productCell = new PdfPCell(new Phrase(item.getProduct().getName()));
                productCell.setPadding(5f);

                PdfPCell quantityCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity())));
                quantityCell.setPadding(5f);

                PdfPCell priceCell = new PdfPCell(new Phrase(String.valueOf(item.getProduct().getSellingPrice())));
                priceCell.setPadding(5f);

                PdfPCell totalCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity() * item.getProduct().getSellingPrice())));
                totalCell.setPadding(5f);

                table.addCell(productCell);
                table.addCell(quantityCell);
                table.addCell(priceCell);
                table.addCell(totalCell);
            });

            document.add(table);

            // Total
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Grand Total: 1500 EGP",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));

            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
