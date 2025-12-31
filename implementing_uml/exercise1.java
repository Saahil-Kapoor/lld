
class PaymentFactory{
    public PaymentStrategy createPayment(String type){
        if(type == "upi"){
            return new UpiPayment();
        }
        else if(type == "credit card"){
            return new CreditCardPayment();
        }
        else if(type == "walle"){
            return new WalletPayment();
        }
        else{
            throw new IllegalArgumentException("no payment method of this type found");
        }
    }
}

class DiscountFactory{
    public DiscountStrategy createDiscount(String type){
        if(type == "coupon"){
            return new CouponDiscount();
        }
        else if(type == "festival"){
            return new FestivalDiscount();
        }
        else if(type == "loyalty"){
            return new LoyaltyDiscount();
        }
        else{
            throw new IllegalArgumentException("No such discount found");
        }
    }
}

interface PaymentStrategy {
    public void pay(double amount);
}

class CreditCardPayment implements PaymentStrategy{
    public void pay(double amount){
        System.out.println("paying using credit card "+amount);
    }
}

class UpiPayment implements PaymentStrategy{
    public void pay(double amount){
        System.out.println("paying using upi "+amount);
    }
}

class WalletPayment implements PaymentStrategy{
    public void pay(double amount){
        System.out.println("paying using wallet "+amount);
    }
}

interface DiscountStrategy{
    public double apply(double amount);
}

class CouponDiscount implements DiscountStrategy{
    public double apply(double amount){
        return amount*0.75;
    }
}

class FestivalDiscount implements DiscountStrategy{
    public double apply(double amount){
        return amount*0.75;
    }
}


class LoyaltyDiscount implements DiscountStrategy{
    public double apply(double amount){
        return amount*0.75;
    }
}

class CheckOutService{
    private PaymentStrategy payment;
    private DiscountStrategy discount;
    public CheckOutService(PaymentStrategy paymentStrategy,DiscountStrategy discountStrategy){
        this.payment = paymentStrategy;
        this.discount = discountStrategy;
    }
    public void checkOut(double amount){
        payment.pay(discount.apply(amount));
    }
}


public class exercise1 {

    public static void main(String[] args) {
        
    }
}