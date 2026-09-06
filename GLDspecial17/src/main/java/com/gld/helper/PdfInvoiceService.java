package com.gld.helper;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.gld.dto.Order;
import com.gld.dto.OrderItem;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class PdfInvoiceService {

    private static final Color BRAND = new Color(92, 23, 37);   // #5c1725
    private static final Color GOLD  = new Color(198, 139, 67); // #c68b43

    public byte[] generateInvoice(Order order) throws Exception {

        Document document = new Document(PageSize.A4, 40, 40, 50, 50);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, baos);

        document.open();

        Font brandFont = new Font(Font.HELVETICA, 22, Font.BOLD, BRAND);
        Font taglineFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
        Font sectionFont = new Font(Font.HELVETICA, 12, Font.BOLD, BRAND);
        Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
        Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.DARK_GRAY);
        Font tableHeaderFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        Font totalFont = new Font(Font.HELVETICA, 14, Font.BOLD, BRAND);

        // ===================== HEADER =====================
        Paragraph brand = new Paragraph("VASTRA MANE", brandFont);
        brand.setAlignment(Element.ALIGN_CENTER);
        document.add(brand);

        Paragraph tagline = new Paragraph("Silk & Sarees  |  Order Invoice", taglineFont);
        tagline.setAlignment(Element.ALIGN_CENTER);
        tagline.setSpacingAfter(18);
        document.add(tagline);

        // ===================== ORDER INFO =====================
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1f, 1f});

        infoTable.addCell(borderlessCell(
                "Order Number: " + safe(order.getOrderNumber()), boldFont));
        infoTable.addCell(borderlessCell(
                "Order Date: " + (order.getOrderDate() != null
                        ? order.getOrderDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
                        : "-"), normalFont));

        document.add(infoTable);
        document.add(Chunk.NEWLINE);

        // ===================== CUSTOMER DETAILS =====================
        Paragraph customerHeading = new Paragraph("Customer / Shipping Details", sectionFont);
        customerHeading.setSpacingAfter(6);
        document.add(customerHeading);

        document.add(new Paragraph("Name: " + safe(order.getCustomerName()), normalFont));
        document.add(new Paragraph("Phone: " + safe(order.getPhone()), normalFont));
        document.add(new Paragraph("Email: " + safe(order.getEmail()) + "  (verified)", normalFont));
        document.add(new Paragraph(
                "Address: " + safe(order.getAddress()) + ", " + safe(order.getCity())
                        + ", " + safe(order.getState()) + " - " + safe(order.getPincode()),
                normalFont));

        document.add(Chunk.NEWLINE);

        // ===================== ITEMS TABLE =====================
        Paragraph itemsHeading = new Paragraph("Order Items", sectionFont);
        itemsHeading.setSpacingAfter(6);
        document.add(itemsHeading);

        PdfPTable table = new PdfPTable(new float[]{3.2f, 1f, 0.7f, 1f, 1.1f});
        table.setWidthPercentage(100);

        addHeaderCell(table, "Item Details", tableHeaderFont);
        addHeaderCell(table, "Type", tableHeaderFont);
        addHeaderCell(table, "Qty", tableHeaderFont);
        addHeaderCell(table, "Price", tableHeaderFont);
        addHeaderCell(table, "Subtotal", tableHeaderFont);

        boolean alt = false;

        for (OrderItem item : order.getItems()) {

            Color rowColor = alt ? new Color(250, 244, 238) : Color.WHITE;
            alt = !alt;

            StringBuilder detail = new StringBuilder();
            detail.append(safe(item.getProductName()));

            if (notEmpty(item.getCategory())) {
                detail.append("\nCategory: ").append(item.getCategory());
            }
            if (notEmpty(item.getFabric())) {
                detail.append("\nFabric: ").append(item.getFabric());
            }
            if (notEmpty(item.getColor())) {
                detail.append("\nColor: ").append(item.getColor());
            }
            if (notEmpty(item.getDesign())) {
                detail.append("\nDesign: ").append(item.getDesign());
            }
            if (notEmpty(item.getWeavingType())) {
                detail.append("\nWeaving: ").append(item.getWeavingType());
            }
            if (notEmpty(item.getExtraInfo())) {
                detail.append("\n").append(item.getExtraInfo());
            }

            table.addCell(bodyCell(detail.toString(), normalFont, rowColor));
            table.addCell(bodyCell(safe(item.getProductType()), normalFont, rowColor));
            table.addCell(bodyCell(String.valueOf(item.getQuantity()), normalFont, rowColor));
            table.addCell(bodyCell("Rs. " + item.getPrice(), normalFont, rowColor));
            table.addCell(bodyCell("Rs. " + item.getSubtotal(), normalFont, rowColor));
        }

        document.add(table);
        document.add(Chunk.NEWLINE);

        // ===================== TOTAL =====================
        Paragraph total = new Paragraph("Total Amount: Rs. " + order.getTotalAmount(), totalFont);
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);

        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        Paragraph thanks = new Paragraph(
                "Thank you for shopping with Vastra Mane! For any queries regarding this order, "
              + "please reply to this email with your order number.",
                new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY));
        document.add(thanks);

        document.close();

        return baos.toByteArray();
    }

    private PdfPCell borderlessCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPaddingBottom(4);
        return cell;
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BRAND);
        cell.setPadding(7);
        table.addCell(cell);
    }

    private PdfPCell bodyCell(String text, Font font, Color background) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(background);
        cell.setPadding(7);
        return cell;
    }

    private String safe(String s) {
        return s == null ? "-" : s;
    }

    private boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

}
