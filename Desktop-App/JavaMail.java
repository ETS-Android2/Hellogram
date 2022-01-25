import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class JavaMail {
    private Properties prop;
    private Session ssn;
    private MimeMessage mm;
    private Transport tr;

    public JavaMail() {
        prop = System.getProperties();
        prop.put("mail.smtp.port", 587);
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        ssn = Session.getDefaultInstance(prop, null);
    }

    public void send(String subject, String to, String messageBody) throws MessagingException {
        mm = new MimeMessage(ssn);
        mm.setSubject(subject);
        mm.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        mm.setContent(messageBody, "text/html;charset=UTF-8");

        tr = ssn.getTransport("smtp");
        tr.connect("smtp.gmail.com", "hellogeram", "M@y12345");
        tr.sendMessage(mm, mm.getAllRecipients());
        tr.close();
    }
}