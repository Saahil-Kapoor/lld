class ShoppingCart{
    private PaymentStrategy paymentStrategy;
    public void setPaymentStrategy(PaymentStrategy paymentStrategy){
        this.paymentStrategy = paymentStrategy;
    }   
    public void checkOut(int amount){
        if(paymentStrategy == null){
            throw new IllegalStateException("No payment strategy");
        }
        paymentStrategy.pay(amount);
    }
}

interface PaymentStrategy{
    public void pay(Integer amount);
}

class CardPayment implements PaymentStrategy{
    public void pay(Integer amount){
        System.out.println("This is card payment strategy");
    }
}

class UpiPayment implements PaymentStrategy{
    public void pay(Integer amount){
        System.out.println("Paying through upi");
    }
}

public class strategy {
    public static void main(String[] args) {
        ShoppingCart cart = new ShoppingCart();
        cart.setPaymentStrategy(new UpiPayment());
        cart.checkOut(500);
    }
}
