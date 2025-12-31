// InvoiceSRPOCP.java
// Messy starter: Monolith Invoice Service (violates SRP + OCP)

import java.util.*;

class LineItem {
    String sku;
    int quantity;
    double unitPrice;

    LineItem(String sku, int quantity, double unitPrice) {
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
}

interface EmailSender{
    void send(String email, String message);
}

interface Logger{
    void log(String message);
}

class ConsoleLogger implements Logger{
    @Override
    public void log(String message){
        System.out.println("[LOG] " + message);
    }
}

class SmtpEmailSender implements EmailSender{
    @Override
    public void send(String email, String message){
        if (email != null && !email.isEmpty()) {
            System.out.println("[SMTP] Sending invoice to " + email + "...");
        }
    }
}

abstract class Discount {
    abstract double calculate(double subtotal);
}

class PercentOffDiscount extends Discount {
    private double percent;

    PercentOffDiscount(double percent) {
        this.percent = percent;
    }

    @Override
    double calculate(double subtotal) {
        return subtotal * (percent / 100.0);
    }
}

class FlatOffDiscount extends Discount {
    private double amount;

    FlatOffDiscount(double amount) {
        this.amount = amount;
    }

    @Override
    double calculate(double subtotal) {
        return amount;
    }
}

class CalculateInvoice {
    public double calculateSubtotal(List<LineItem> items) {
        double subtotal = 0;
        for (LineItem it : items) {
            subtotal += it.unitPrice * it.quantity;
        }
        return subtotal;
    }

    public double calculateTax(double amount) {
        return amount * 0.18;
    }

    public double calculateGrandTotal(double subtotal, double discountTotal, double tax) {
        return subtotal - discountTotal + tax;
    }

    public double calculateDiscounts(List<LineItem> items, Set<Discount> discounts,double subtotal) {
        // logic to calculate discounts
        double discountTotal = 0.0;
        for (Discount discount : discounts) {

            /*
             * if (k.equals("percent_off")) {
             * discountTotal += subtotal * (v / 100.0);
             * } else if (k.equals("flat_off")) {
             * discountTotal += v;
             * } else {
             * // unknown ignored
             * }
             */
            discountTotal += discount.calculate(subtotal);
        }
        return discountTotal;

    }

    
}


class InvoiceProcessing{
    StringBuilder pdf;
    public EmailSender emailSender;
    public Logger logger;
    public InvoiceProcessing(EmailSender emailSender,Logger logger) {
        pdf = new StringBuilder();
        this.emailSender = emailSender;
        this.logger = logger;
    }
    public void appendItems(List<LineItem> items){
        pdf.append("INVOICE\n");
        for (LineItem it : items) {
            pdf.append(it.sku).append(" x").append(it.quantity).append(" @ ").append(it.unitPrice).append("\n");
        }
    }
    public void appendTotals(double subtotal, double discountTotal, double tax, double grand){
        pdf.append("Subtotal: ").append(subtotal).append("\n")
                .append("Discounts: ").append(discountTotal).append("\n")
                .append("Tax: ").append(tax).append("\n")
                .append("Total: ").append(grand).append("\n");
    }

    public void logging(String email, double grand){
        System.out.println("[LOG] Invoice processed for " + email + " total=" + grand);
    }
    public String processInvoice(List<LineItem> items, double subtotal, double discountTotal, double tax, double grand,
            String email) {
        // logic to save invoice data
        
        appendItems(items);
        appendTotals(subtotal, discountTotal, tax, grand);

        // email I/O inline (tight coupling)
        emailSender.send(email, pdf.toString());

        // logging inline
        logger.log("Invoice processed for " + email + " total=" + grand);
        return pdf.toString();
    }
}

class InvoiceService {
    // TO FIX (SRP): Does pricing, discounting, tax, rendering, email, logging.
    // TO FIX (OCP): Discount types hard-coded with if/else.
    String process(List<LineItem> items, Set<Discount> discounts, String email) {
        EmailSender emailSender = new SmtpEmailSender();
        Logger logger = new ConsoleLogger();
        // pricing
        CalculateInvoice processor = new CalculateInvoice();
        InvoiceProcessing invoiceProcessor = new InvoiceProcessing(emailSender, logger  );
        double subtotal = processor.calculateSubtotal(items);

        // discounts (tightly coupled)
        double discountTotal = processor.calculateDiscounts(items, discounts,subtotal);

        // tax inline
        double tax = processor.calculateTax(subtotal - discountTotal);
        double grand = processor.calculateGrandTotal(subtotal, discountTotal, tax);

        // rendering inline (pretend PDF)

        return invoiceProcessor.processInvoice(items, subtotal, discountTotal, tax, grand, email);
    }

    // helper used by ad-hoc tests; also messy on purpose
    double computeTotal(List<LineItem> items, Set<Discount> discounts) {
        String rendered = process(items, discounts, "noreply@example.com");
        int idx = rendered.lastIndexOf("Total:");
        if (idx < 0)
            throw new RuntimeException("No total");
        String num = rendered.substring(idx + 6).trim();
        return Double.parseDouble(num);
    }
}

public class InvoiceSRPOCP {
    public static void main(String[] args) {
        InvoiceService svc = new InvoiceService();
        List<LineItem> items = Arrays.asList(
                new LineItem("BOOK-001", 2, 500.0),
                new LineItem("USB-DRIVE", 1, 799.0));
        Set<Discount> discounts = new HashSet<>();
        discounts.add(new PercentOffDiscount(10.0));
        System.out.println(svc.process(items, discounts, "customer@example.com"));
    }
}
