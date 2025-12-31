interface Mailer {
    void send(String templ, String to, String body);
}
class SmtpMailer implements Mailer {
    @Override
    public void send(String templ, String to, String body) {
        System.out.println("[SMTP] " + templ + " -> " + to);
    }
}

interface SmsClient {
    void sendOTP(String phone, String code);
}
class TwilioClient implements SmsClient {
    @Override
    public void sendOTP(String phone, String code) {
        System.out.println("[Twilio] OTP " + code + " -> " + phone);
    }
}
class User {
    String email;
    String phone;
    User(String email, String phone) { this.email = email; this.phone = phone; }
}

class SignUpService {
    private final Mailer mailer;
    private final SmsClient smsClient;
    SignUpService(Mailer mailer, SmsClient smsClient){
        this.mailer = mailer;
        this.smsClient = smsClient;
    }

    boolean signUp(User u){
        if (u.email == null || u.email.isEmpty()) return false;
        // pretend DB save here…


        mailer.send("welcome", u.email, "Welcome!");

        smsClient.sendOTP(u.phone, "123456");
        return true;
    }
}

public class NotifyDIPOCP {
    public static void main(String[] args) {
        Mailer mailer = new SmtpMailer();
        SmsClient sms = new TwilioClient();
        SignUpService svc = new SignUpService(mailer, sms);
        svc.signUp(new User("user@example.com", "+15550001111"));
    }
}
