interface INotification{
    void send(String to,String message);
}

class EmailNotification implements INotification{
    public void send(String to,String message){
        System.out.println("this is a email notification");
    }
}

abstract class NotificationDecorator implements INotification{
    INotification wrapee;
    NotificationDecorator(INotification n){
        this.wrapee = n;
    }
    public void send(String to,String message){
        wrapee.send(to, message);
    }
}

class LoggingNotification extends NotificationDecorator{
    public LoggingNotification(INotification wrapee){
        super(wrapee);
    }
    public void send(String to,String message){
        System.out.println("this is logging to");
        super.send(to,message);
        System.out.println("logging ending is to end");
    }
}


public class Decorator {
    public static void main(String[] args) {
           INotification notification = new LoggingNotification(new EmailNotification());
           notification.send("saahil@mail.com","this is error/log");
    }
}
