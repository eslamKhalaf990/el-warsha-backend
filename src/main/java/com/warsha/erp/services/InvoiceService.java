package com.warsha.erp.services;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.warsha.erp.entities.Invoice;
import com.warsha.erp.entities.Order;
import com.warsha.erp.entities.OrderItems;
import com.warsha.erp.repository.InvoiceRepository;
import com.warsha.erp.repository.OrderRepository;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Stream;

@Service
public class InvoiceService {
    final InvoiceRepository invoiceRepository;
    final OrderRepository orderRepository;
    final PaymentService paymentService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          OrderRepository orderRepository, PaymentService paymentService) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
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
            Document document = createDocument(baos);

            // Add sections
            document.add(createHeader(invoice));
            document.add(Chunk.NEWLINE);
            document.add(createItemsTable(invoice));
            document.add(createTotalTable(invoice));
            document.add(Chunk.NEWLINE);
            document.add(createFooter(invoice));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    // ====== Helpers ======
    private Document createDocument(ByteArrayOutputStream baos) throws DocumentException {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, baos);
        document.open();
        return document;
    }

    private PdfPTable createHeader(Invoice invoice) throws Exception {
        Font normalFont = FontFactory.getFont(FontFactory.TIMES_BOLDITALIC, 12);

        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        // Left Cell: Invoice Info
        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(Rectangle.NO_BORDER);
        Paragraph invoiceNumber = new Paragraph("Invoice #" + invoice.getId(),
                FontFactory.getFont(FontFactory.TIMES_BOLDITALIC, 20));
        invoiceNumber.setAlignment(Element.ALIGN_CENTER);
        textCell.addElement(invoiceNumber);

        Paragraph issuedDate = new Paragraph("Issued Date: " + invoice.getIssuedDate(), normalFont);
        issuedDate.setAlignment(Element.ALIGN_CENTER);
        textCell.addElement(issuedDate);

        // Right Cell: Logo
        Image logo = Image.getInstance("https://drive.usercontent.google.com/download?id=10nH5QZg3EebwUmbXSB0P4A0xoNFmo2Lg&export=view&authuser=0");
        logo.scaleToFit(100, 100);
        PdfPCell logoCell = new PdfPCell(logo);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        headerTable.addCell(logoCell);
        headerTable.setSpacingAfter(30f);
        headerTable.addCell(textCell);

        headerTable.setSpacingAfter(20f);
        return headerTable;
    }

    private PdfPTable createItemsTable(Invoice invoice) throws DocumentException {
        Font boldFont = FontFactory.getFont(FontFactory.TIMES_BOLDITALIC, 12);
        Font normalFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 12);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{50, 15, 15, 20});

        Stream.of("Product", "Quantity", "Price", "Total").forEach(col -> {
            PdfPCell header = new PdfPCell(new Phrase(col, boldFont));
            header.setBorder(Rectangle.BOTTOM);
            if (col.equals("Product")) header.setHorizontalAlignment(Element.ALIGN_LEFT);
            else header.setHorizontalAlignment(Element.ALIGN_RIGHT);

            header.setBorderWidthBottom(0.7f);

            // Add spacing above and below the border
            header.setPaddingTop(8f);
            header.setPaddingBottom(20f);

            table.addCell(header);
        });

        // Data Rows
        List<OrderItems> items = invoice.getOrder().getItems();

        for (int i = 0; i < items.size(); i++) {
            OrderItems item = items.get(i);

            PdfPCell productCell = createCell(item.getProduct().getName(), normalFont, Element.ALIGN_LEFT);
            PdfPCell qtyCell = createCell(String.valueOf(item.getQuantity()), normalFont, Element.ALIGN_RIGHT);
            PdfPCell priceCell = createCell(item.getProduct().getSellingPrice() + " EGP", normalFont, Element.ALIGN_RIGHT);
            PdfPCell totalCell = createCell((item.getQuantity() * item.getProduct().getSellingPrice()) + " EGP", normalFont, Element.ALIGN_RIGHT);

            // Add spacing only for the first row (to push it away from the header line)
            if (i == 0) {
                productCell.setPaddingTop(20f);
                qtyCell.setPaddingTop(20f);
                priceCell.setPaddingTop(20f);
                totalCell.setPaddingTop(20f);
            }

            table.addCell(productCell);
            table.addCell(qtyCell);
            table.addCell(priceCell);
            table.addCell(totalCell);
        }


        return table;
    }

    private PdfPCell createCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setPaddingTop(4f);
        cell.setPaddingBottom(3f);
        return cell;
    }

    private PdfPTable createTotalTable(Invoice invoice) throws DocumentException {
        Font smallBoldFont = FontFactory.getFont(FontFactory.TIMES_BOLDITALIC, 12);

        // 2-column table
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);
        totalTable.setWidths(new float[]{2f, 1.5f});
        totalTable.setSpacingBefore(10f);

        // ========== LEFT COLUMN ==========
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        leftCell.setPaddingTop(16f); // spacer above

        leftCell.addElement(new Paragraph("Total Price: " + invoice.getOrder().getTotalPrice() + " EGP", smallBoldFont));
        leftCell.addElement(new Paragraph("Delivery Charge: " + invoice.getOrder().getDeliveryCharge() + " EGP", smallBoldFont));
        leftCell.addElement(new Paragraph("Discount: " + invoice.getOrder().getDiscount() + " EGP", smallBoldFont));
        leftCell.addElement(new Paragraph("Amount Paid: " + paymentService.getPaymentsByOrder(invoice.getOrder().getId()).get(0).getAmountPaid() + " EGP", smallBoldFont));

        // ========== RIGHT COLUMN ==========
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightCell.setPaddingTop(16f); // spacer above

        Paragraph shippingTo = new Paragraph("Shipping To: " + invoice.getOrder().getCustomer().getFullName(), smallBoldFont);
        shippingTo.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(shippingTo);

        Paragraph phone = new Paragraph("Phone: " + invoice.getOrder().getCustomer().getPhone(), smallBoldFont);
        phone.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(phone);

        Paragraph address = new Paragraph("Address: " + invoice.getOrder().getCustomer().getAddress(), smallBoldFont);
        address.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(address);

        // Add cells to table (one row, two columns)
        totalTable.addCell(leftCell);
        totalTable.addCell(rightCell);

        return totalTable;
    }

    private Element createFooter(Invoice invoice) throws DocumentException {

        // Fonts
        Font italicBoldFont = FontFactory.getFont(FontFactory.TIMES_BOLDITALIC, 12); // bold + italic
        Font italicFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 10); // italic

        // One column table (full width)
        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setWidthPercentage(100);
        footerTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        // Spacer (empty row above footer content)
        PdfPCell spacer = new PdfPCell(new Phrase(" "));
        spacer.setBorder(Rectangle.NO_BORDER);
        spacer.setFixedHeight(30f); // adjust for more/less space
        footerTable.addCell(spacer);

        // Thank you + Terms cell
        PdfPCell footerCell = new PdfPCell();
        footerCell.setBorder(Rectangle.NO_BORDER);
        footerCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph thankYou = new Paragraph("Thank you for your order!", italicBoldFont);
        thankYou.setAlignment(Element.ALIGN_CENTER);
        footerCell.addElement(thankYou);

        Paragraph terms = new Paragraph("Terms and Conditions\nThere is no return policy.", italicFont);
        terms.setAlignment(Element.ALIGN_CENTER);
        footerCell.addElement(terms);

        footerTable.addCell(footerCell);

        return footerTable;
    }
}