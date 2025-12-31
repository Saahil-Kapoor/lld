interface OrderState{
    public void next(OrderContext context);
    public void cancel(OrderContext context);
}

class OrderedState implements OrderState{
    public void next(OrderContext context){
        System.out.println("order placed, waiting for shipping");
        context.setState(new ShippedState());
    }
    public void cancel(OrderContext context){
        System.out.println("order is cancelled");
        context.setState(new CancelledState());
    }
}

class ShippedState implements OrderState{
    public void next(OrderContext context){
        System.out.println("Order is shipped, waiting for dilevery");
        context.setState(new DeliveredState());
    }
    public void cancel(OrderContext context){
        System.out.println("Order is shipped , cannot cancel");
    }
    
}
class DeliveredState implements OrderState{
    public void next(OrderContext context){
        System.out.println("Order is delivered, Hope you enjoy the product");
        context.setState(new DeliveredState());
    }
    public void cancel(OrderContext context){
        System.out.println("Cannot cancel , order already delivered");
    }
}

class CancelledState implements OrderState{
    public void next(OrderContext context){
        System.out.println("cannot proceed , please order again. This Order is already cancelled");
    }
    public void cancel(OrderContext context){
        System.out.println("Order is already cancelled");
    }
}

class OrderContext{
    OrderState state;
    OrderContext(){
        this.state = new OrderedState();
    }
    void setState(OrderState state){
        this.state = state;
    }
    void proceedNext(){
        state.next(this);
    }
    void cancel(){
        state.cancel(this);
    }
}


//this is client code
public class StatePatternDelivery {

    public static void main(String[] args) {
        OrderContext order1 = new OrderContext();
        order1.proceedNext();
        order1.cancel();
        order1.proceedNext();
        order1.proceedNext();
        order1.proceedNext();
        
    }
}