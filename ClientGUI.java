import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClientGUI {

    private JFrame frame;
    private JTextPane chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JLabel statusLabel;
    private PrintWriter out;
    private String username;
    
    // Modern color scheme
    private final Color PRIMARY_COLOR = new Color(37, 99, 235);
    private final Color SECONDARY_COLOR = new Color(243, 244, 246);
    private final Color ACCENT_COLOR = new Color(16, 185, 129);
    private final Color TEXT_COLOR = new Color(31, 41, 55);
    private final Color LIGHT_GRAY = new Color(249, 250, 251);

    public ClientGUI() {
        askUsername();
        buildUI();
        connectToServer();
    }

    // Ask user for username on startup
    private void askUsername() {
        username = JOptionPane.showInputDialog(null,
                "Enter your username:",
                "Login",
                JOptionPane.PLAIN_MESSAGE);

        if (username == null || username.trim().isEmpty()) {
            username = "Unknown";
        }
    }

    private void buildUI() {
        frame = new JFrame("💬 Chat App - " + username);
        frame.setSize(500, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(LIGHT_GRAY);
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        frame.add(headerPanel, BorderLayout.NORTH);

        // Chat display area
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBackground(Color.WHITE);
        chatArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = createInputPanel();
        frame.add(inputPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
        inputField.requestFocus();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("💬 Chat Room");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel userLabel = new JLabel("👤 " + username);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        
        statusLabel = new JLabel("🟢 Connected");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(ACCENT_COLOR);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.add(userLabel, BorderLayout.NORTH);
        rightPanel.add(statusLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(LIGHT_GRAY);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));
        
        // Input field with modern styling
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        inputField.setBackground(Color.WHITE);
        
        // Send button with modern styling
        sendButton = new JButton("Send 📤");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(PRIMARY_COLOR);
        sendButton.setForeground(Color.WHITE);
        sendButton.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect for send button
        sendButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                sendButton.setBackground(new Color(29, 78, 216));
            }
            public void mouseExited(MouseEvent e) {
                sendButton.setBackground(PRIMARY_COLOR);
            }
        });
        
        // Action listeners
        ActionListener sendAction = e -> sendMessage();
        inputField.addActionListener(sendAction);
        sendButton.addActionListener(sendAction);
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(Box.createHorizontalStrut(10), BorderLayout.LINE_END);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        return inputPanel;
    }
    
    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty() && out != null) {
            String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            out.println("[" + timestamp + "] " + username + ": " + msg);
            inputField.setText("");
        }
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            // Thread to read messages from server
            Thread readThread = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        final String message = msg;
                        SwingUtilities.invokeLater(() -> {
                            appendMessage(message);
                            chatArea.setCaretPosition(chatArea.getDocument().getLength());
                        });
                    }
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("🔴 Disconnected");
                        statusLabel.setForeground(Color.RED);
                        appendMessage("❌ Disconnected from server");
                    });
                }
            });

            readThread.start();

        } catch (IOException e) {
            statusLabel.setText("🔴 Connection Failed");
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(frame,
                    "Cannot connect to server!\nPlease make sure the server is running.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
    
    private void appendMessage(String message) {
        try {
            javax.swing.text.StyledDocument doc = chatArea.getStyledDocument();
            
            // Create styles for different message types
            javax.swing.text.Style defaultStyle = chatArea.addStyle("default", null);
            javax.swing.text.StyleConstants.setFontFamily(defaultStyle, "Segoe UI");
            javax.swing.text.StyleConstants.setFontSize(defaultStyle, 14);
            javax.swing.text.StyleConstants.setForeground(defaultStyle, TEXT_COLOR);
            
            javax.swing.text.Style myMessageStyle = chatArea.addStyle("myMessage", defaultStyle);
            javax.swing.text.StyleConstants.setForeground(myMessageStyle, PRIMARY_COLOR);
            javax.swing.text.StyleConstants.setBold(myMessageStyle, true);
            
            javax.swing.text.Style systemStyle = chatArea.addStyle("system", defaultStyle);
            javax.swing.text.StyleConstants.setForeground(systemStyle, new Color(107, 114, 128));
            javax.swing.text.StyleConstants.setItalic(systemStyle, true);
            
            // Determine message style
            javax.swing.text.Style style = defaultStyle;
            if (message.contains(username + ":")) {
                style = myMessageStyle;
            } else if (message.startsWith("❌") || message.startsWith("✅")) {
                style = systemStyle;
            }
            
            doc.insertString(doc.getLength(), message + "\n\n", style);
            
        } catch (Exception e) {
            chatArea.setText(chatArea.getText() + message + "\n\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}