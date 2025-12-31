interface IPaymentProcessor{
    public void processPayment(double amount);
}

class OldPaymentAdapter implements IPaymentProcessor{
    private OldPaymentService service;
    public OldPaymentAdapter(OldPaymentService service){
        this.service = service;
    }
    public void processPayment(double amountInRupees){
        double amountPaise = amountInRupees*100f;
        service.makePaymentInPaise(amountPaise);
        System.out.println("processing the payment");
    }
}

class OldPaymentService{
    public void makePaymentInPaise(double paise){
        System.out.println("processing payment in paise");
    }
}

public class adapter {
    public static void main(String[] args) {
        OldPaymentService ops = new OldPaymentService();
        IPaymentProcessor oldprocessor = new OldPaymentAdapter(ops);
        oldprocessor.processPayment(499.99);

    }
}
