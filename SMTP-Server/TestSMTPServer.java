import java.sql.SQLException;
import java.util.List;

public class TestSMTPServer {
    public static void main(String[] args) throws SQLException {
        SMTPServer server = new SMTPServer();
        List<Email> emails = server.retrieveDatabase();

        // Print out the emails to verify the query is working correctly
        for (Email email : emails) {
            System.out.println("From: " + email.getFrom());
            System.out.println("To: " + email.getTo());
            System.out.println("Subject: " + email.getSubject());
            System.out.println("Message: " + email.getMessage());
            System.out.println("Date: " + email.getDate());
            System.out.println("Attachments: " + email.getAttachments().size());
            System.out.println("------------------------");
        }
    }
}
