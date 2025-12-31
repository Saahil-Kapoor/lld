interface INotification {
    public void send(String to, String message);
}

class EmailNotification implements INotification {
    public void send(String to, String message) {
        System.out.println("This is an email notification");
    }
}

class SmsNotification implements INotification {
    public void send(String to, String message) {
        System.out.println("This is a sms notification");
    }
}

class PushNotification implements INotification {
    public void send(String to, String message) {
        System.out.println("this is a push notification");
    }
}

class NotificationFactory{
    public static INotification create(String channel){
        String lowered = channel.toLowerCase();
        if( lowered == "email"){
            return new EmailNotification();
        }
        else if(lowered == "sms"){
            return new SmsNotification();
        }
        else if(lowered == "push"){
            return new PushNotification();
        }
        else{
            throw new IllegalArgumentException("unknown argument found");
        }
    }
}

public class factory {
    public static void main(String[] args) {
        INotification notification = NotificationFactory.create("email");
        notification.send("user@example.com","Hello");
        
    }
}