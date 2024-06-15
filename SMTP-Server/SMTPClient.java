import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.List;

public class SMTPClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public SMTPClient() throws IOException {
        socket = new Socket("localhost", 6424);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println(in.readLine()); // Read the server's initial response
    }

    public boolean authorize(String username, String password) throws IOException {
        out.println("EHLO localhost");
        System.out.println(in.readLine()); // Read server response
        String authString = "\u0000" + username + "\u0000" + password;
        String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes());
        out.println("AUTH PLAIN " + encodedAuthString);

        String response = in.readLine();
        return response.startsWith("235");
    }
    public void sendEmail(String from, String to, String subject, String message, List<File> attachments) throws IOException {
        out.println("MAIL FROM:<" + from + ">");
        System.out.println(in.readLine()); // Read server response

        out.println("RCPT TO:<" + to + ">");
        System.out.println(in.readLine()); // Read server response

        out.println("DATA");
        System.out.println(in.readLine()); // Read server response

        out.println("From: " + from);
        out.println("To: " + to);
        out.println("Subject: " + subject);
        out.println("Message: " + message.replaceAll("\n", " ")); // Only send the first line of the message

        for (File attachment : attachments) {
            if (attachment != null) {
                out.println("--boundary");
                out.println("Content-Type: application/octet-stream; name=\"" + attachment.getName() + "\"");
                out.println("Content-Transfer-Encoding: base64");
                out.println("Content-Disposition: attachment; filename=\"" + attachment.getName() + "\"");
                try (FileInputStream fileInputStream = new FileInputStream(attachment);
                     ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                    }

                    String encodedFileContent = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
                    out.println(encodedFileContent);
                }
            }
        }

        out.println(".");
        System.out.println(in.readLine()); // Read server response
    }

    public void quit() throws IOException {
        out.println("QUIT");
        System.out.println(in.readLine()); // Read server response
        socket.close();
    }

    public static void main(String[] args) {
        // Example usage
        try {
            SMTPClient client = new SMTPClient();
            if (client.authorize("sgvt@gmail.com", "123")) {
                System.out.println("Authorization successful!");
                client.sendEmail("sgvt@gmail.com", "recipient@example.com", "Test Subject", "This is a test message.", null);
                client.quit();
            } else {
                System.out.println("Authorization failed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
