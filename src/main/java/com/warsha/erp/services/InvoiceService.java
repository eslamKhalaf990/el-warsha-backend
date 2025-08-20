package com.warsha.erp.services;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.warsha.erp.entities.Invoice;
import com.warsha.erp.entities.Order;
import com.warsha.erp.repository.InvoiceRepository;
import com.warsha.erp.repository.OrderRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.*;
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
            Document document = new Document(PageSize.A4, 36, 36, 36, 36); // add margins
            PdfWriter.getInstance(document, baos);

            document.open();

            // ===== Header Table (Text + Logo) =====
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{70, 30});

            // Left Cell: Invoice Info
            PdfPCell textCell = new PdfPCell();
            textCell.setBorder(Rectangle.NO_BORDER);

            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            textCell.addElement(new Paragraph("Invoice #" + invoice.getId(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            textCell.addElement(new Paragraph("Date: " + invoice.getIssuedDate().toString(), normalFont));
            textCell.addElement(new Paragraph("Customer: " + invoice.getOrder().getCustomer().getFullName(), normalFont));
            textCell.addElement(new Paragraph("Phone: " + invoice.getOrder().getCustomer().getPhone(), normalFont));
            textCell.addElement(new Paragraph("Address: " + invoice.getOrder().getCustomer().getAddress(), normalFont));

            // Logo (Right cell)
            Image logo = Image.getInstance("https://drive.usercontent.google.com/download?id=10nH5QZg3EebwUmbXSB0P4A0xoNFmo2Lg&export=view&authuser=0");
            logo.scaleToFit(100, 100);

            PdfPCell logoCell = new PdfPCell(logo);
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            logoCell.setVerticalAlignment(Element.ALIGN_TOP);

            headerTable.addCell(textCell);
            headerTable.addCell(logoCell);

            headerTable.setSpacingAfter(20f);
            document.add(headerTable);

            // ===== Items Table =====
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(30f);
            table.setWidths(new float[]{50, 15, 15, 20});

            // Header row
            Stream.of("Product", "Quantity", "Price", "Total").forEach(columnTitle -> {
                PdfPCell header = new PdfPCell(new Phrase(columnTitle, boldFont));
                header.setHorizontalAlignment(Element.ALIGN_LEFT); // left aligned
                header.setPaddingBottom(30f);  // more bottom padding for spacing
                header.setBorder(Rectangle.BOTTOM); // only bottom border
                header.setBorderWidthBottom(0.7f);  // slightly thicker divider line
                header.setBorderColorBottom(Color.GRAY);
                table.addCell(header);
            });

            document.add(Chunk.NEWLINE);

            // Data rows (no borders)
            invoice.getOrder().getItems().forEach(item -> {
                PdfPCell productCell = new PdfPCell(new Phrase(item.getProduct().getName(), normalFont));
                productCell.setBorder(Rectangle.NO_BORDER);
                productCell.setPaddingTop(8f);   // spacing between header and items
                productCell.setPaddingBottom(6f);

                PdfPCell quantityCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                quantityCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                quantityCell.setBorder(Rectangle.NO_BORDER);
                quantityCell.setPaddingTop(8f);
                quantityCell.setPaddingBottom(6f);

                PdfPCell priceCell = new PdfPCell(new Phrase(String.valueOf(item.getProduct().getSellingPrice()), normalFont));
                priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                priceCell.setBorder(Rectangle.NO_BORDER);
                priceCell.setPaddingTop(8f);
                priceCell.setPaddingBottom(6f);

                PdfPCell totalCell = new PdfPCell(new Phrase(
                        String.valueOf(item.getQuantity() * item.getProduct().getSellingPrice()), normalFont));
                totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                totalCell.setBorder(Rectangle.NO_BORDER);
                totalCell.setPaddingTop(8f);
                totalCell.setPaddingBottom(6f);

                table.addCell(productCell);
                table.addCell(quantityCell);
                table.addCell(priceCell);
                table.addCell(totalCell);
            });

            document.add(table);

            // grand total
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);
            totalTable.setWidths(new float[]{80, 20});

            PdfPCell empty = new PdfPCell(new Phrase(""));
            empty.setBorder(Rectangle.NO_BORDER);

            PdfPCell totalCell = new PdfPCell(new Phrase("Grand Total: 1500 EGP", boldFont));
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalCell.setBorder(Rectangle.TOP);
            totalCell.setPadding(8f);

            totalTable.addCell(empty);
            totalTable.addCell(totalCell);

            // ===== Footer Section =====
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // Thank you message
            Paragraph thankYou = new Paragraph("Thank you for your business!",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            thankYou.setAlignment(Element.ALIGN_CENTER);
            document.add(thankYou);

            document.add(Chunk.NEWLINE);

            // Delivery & Down Payment
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{70, 30});

            // Left: Notes
            PdfPCell notesCell = new PdfPCell();
            notesCell.setBorder(Rectangle.NO_BORDER);
            notesCell.addElement(new Paragraph("Delivery Charge: 50 EGP", normalFont));
            notesCell.addElement(new Paragraph("Down Payment: 200 EGP", normalFont));
            footerTable.addCell(notesCell);

            // Right: Empty (for spacing)
            PdfPCell emptyRight = new PdfPCell(new Phrase(""));
            emptyRight.setBorder(Rectangle.NO_BORDER);
            footerTable.addCell(emptyRight);

            document.add(footerTable);

            // Terms & Conditions
            Paragraph terms = new Paragraph(
                    "Terms & Conditions:\n- No return policy after 30 days.",
                    FontFactory.getFont(FontFactory.HELVETICA, 10));
            terms.setAlignment(Element.ALIGN_LEFT);


            document.add(totalTable);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            document.add(terms);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}

class BackgroundEvent extends PdfPageEventHelper {
    @Override
    public void onEndPage(PdfWriter writer, com.lowagie.text.Document document) {
        PdfContentByte canvas = writer.getDirectContentUnder();
        Rectangle rect = document.getPageSize();
        canvas.setColorFill(new Color(240, 240, 240)); // light grey
        canvas.rectangle(rect.getLeft(), rect.getBottom(), rect.getWidth(), rect.getHeight());
        canvas.fill();
    }
}
