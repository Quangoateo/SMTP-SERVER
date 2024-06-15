import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SMTPClientGUI extends JFrame implements ActionListener {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JLabel backgroundLabel;
    private final SMTPClient smtpClient;
    public   SMTPServer smtpServer;
    public SMTPClientGUI() throws IOException {
        setTitle("Login Page");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        ImageIcon backgroundImage = new ImageIcon("background.jpg");
        backgroundLabel = new JLabel(backgroundImage);
        backgroundLabel.setLayout(null);
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10) ;

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(15);
        usernameLabel.setForeground(Color.WHITE);
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(15);
        passwordLabel.setForeground(Color.WHITE);
        loginButton = new JButton("Login");
        usernameField.setBackground(Color.WHITE);
        passwordField.setBackground(Color.WHITE);
        loginButton.setBackground(Color.WHITE);
        loginButton.addActionListener(this);

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);

        loginPanel.setBounds(150, 90, 320, 200);
        backgroundLabel.add(loginPanel);
        setContentPane(backgroundLabel);

        smtpClient = new SMTPClient();
        smtpServer = new SMTPServer();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (authenticate(username, password)) {
                try {
                    showMessagePage(username);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private boolean authenticate(String username, String password) {
        try {
            return smtpClient.authorize(username, password);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private JTable createSentMessagesTable() throws SQLException {
    // Define column names
    String[] columnNames = {"Recipient", "Subject", "Message"};
    // Get sent messages data
     List<Email> emails  = smtpServer.retrieveDatabase();

     Object[][] data = new Object[emails.size()][3];
        for (int i = 0; i < emails.size(); i++) {
            Email email = emails.get(i);
            data[i][0] = email.getTo();
            data[i][1] = email.getSubject();
            data[i][2] = email.getMessage();
        }
    // Create table with data and column names
    JTable table = new JTable(data, columnNames);
    return table;
}

    private void showMessagePage(String username) throws SQLException {
    JFrame sendMessageFrame  = new JFrame("Send Message Page");
    sendMessageFrame.setSize(500, 500);
    sendMessageFrame.setLocationRelativeTo(null);

    // Create tabs for inbox and sent messages
    JTabbedPane tabbedPane = new JTabbedPane();

    // Send message panel
    JPanel sendMessagePanel = new JPanel();
    sendMessagePanel.setLayout(new BoxLayout(sendMessagePanel, BoxLayout.Y_AXIS));
    sendMessagePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

    // Input panel for recipient, subject and message
    JPanel inputPanel = new JPanel(new GridLayout(3, 1, 0, 10));

    // Recipient field
    JTextField recipientField = new JTextField();
    inputPanel.add(createLabeledTextField("Recipient:", recipientField, 200, 20));

    // Subject field
    JTextField subjectField = new JTextField();
    inputPanel.add(createLabeledTextField("Subject:", subjectField, 200, 20));

    // Message area
    JTextArea messageArea = new JTextArea();
    messageArea.setPreferredSize(new Dimension(200, 20));
    inputPanel.add(createLabeledTextArea("Message:", messageArea, 200, 170));

    // Add input panel to the send message panel
    sendMessagePanel.add(inputPanel);

    // Create sent messages table
    JTable sentMessagesTable = createSentMessagesTable();

    // Create a scroll pane and add the table to it
    JScrollPane sentMessagesScrollPane = new JScrollPane(sentMessagesTable);

    // Add the scroll pane to a new tab
    tabbedPane.addTab("Sent Messages", sentMessagesScrollPane);

    // Add the tabbed pane to the send message panel
    sendMessagePanel.add(tabbedPane);

    // File attachment field
    JPanel attachmentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton attachButton = new JButton("Attach Files");
    JLabel fileLabel = new JLabel("No files attached");
    List<File> attachments = new ArrayList<>();

    // Choose files
    attachButton.addActionListener(e -> {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        int result = fileChooser.showOpenDialog(sendMessageFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            for (File file : selectedFiles) {
                attachments.add(file);
            }
            fileLabel.setText("Attached: " + attachments.size() + " files");
        }
    });

    attachmentPanel.add(attachButton);
    attachmentPanel.add(fileLabel);

    // Submit button
    JButton submitButton = new JButton("Submit");
    submitButton.addActionListener(e -> {
        String recipient = recipientField.getText();
        String subject = subjectField.getText();
        String message = messageArea.getText();
        try {
            smtpClient.sendEmail(username, recipient, subject, message, attachments);
            JOptionPane.showMessageDialog(sendMessageFrame, "Email sent successfully");
            //  Refresh the sent messages table
            JTable newTable = createSentMessagesTable();
            sentMessagesScrollPane.setViewportView(newTable);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(sendMessageFrame, "Failed to send email", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    });

    // Add attachment panel and submit button to a new panel
    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.add(attachmentPanel, BorderLayout.NORTH);
    buttonPanel.add(submitButton, BorderLayout.SOUTH);

    // Add button panel to the send message panel
    sendMessagePanel.add(buttonPanel);

    // Add send message panel to the frame
    sendMessageFrame.add(sendMessagePanel);

    // Make the frame visible
    sendMessageFrame.setVisible(true);
}

    private JPanel createLabeledTextField(String label, JTextField textField, int width, int height) {
        JPanel panel = new JPanel(new BorderLayout(10, 1));
        JLabel jLabel = new JLabel(label);

        // Set the preferred size of the text field
        textField.setPreferredSize(new Dimension(width, height));

        panel.add(jLabel, BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLabeledTextArea(String label, JTextArea textArea, int width, int height) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        JLabel jLabel = new JLabel(label);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(width, height));

        // Adjust the height of the label area by specifying BorderLayout.NORTH
        panel.add(jLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);  // Keep the scroll pane in the center

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SMTPClientGUI gui = null;
            try {
                gui = new SMTPClientGUI();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            gui.setVisible(true);
        });
    }
}
