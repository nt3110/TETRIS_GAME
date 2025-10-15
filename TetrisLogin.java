import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

// Login and Sign Up window for Tetris game
public class TetrisLogin extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton signupButton;
    private JLabel statusLabel;
    private Map<String, String> userDatabase = new HashMap<>();

    public TetrisLogin() {
        setTitle("Tetris Login");
        setSize(320, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);
        loginButton = new JButton("Login");
        signupButton = new JButton("Sign Up");
        panel.add(loginButton);
        panel.add(signupButton);
        statusLabel = new JLabel("");
        panel.add(statusLabel);
        add(panel, BorderLayout.CENTER);

        // Default account for demo
        userDatabase.put("player", "tetris");

        // Login button action
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                if (authenticate(username, password)) {
                    dispose(); // Close login window
                    launchTetris();
                } else {
                    statusLabel.setText("Invalid login!");
                }
            }
        });

        // Sign Up button action
        signupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                if (username.isEmpty() || password.isEmpty()) {
                    statusLabel.setText("Enter username and password");
                } else if (userDatabase.containsKey(username)) {
                    statusLabel.setText("Username already exists");
                } else {
                    userDatabase.put(username, password);
                    statusLabel.setText("Sign up successful! You can log in.");
                }
            }
        });
    }

    // Authentication using in-memory user database
    private boolean authenticate(String username, String password) {
        return userDatabase.containsKey(username) && userDatabase.get(username).equals(password);
    }

    // Launch the Tetris game after successful login
    private void launchTetris() {
        JFrame frame = new JFrame("Tetris");
        JLabel statusbar = new JLabel(" 0");
        frame.add(statusbar, BorderLayout.SOUTH);
        Tetris game = new Tetris(frame);
        frame.add(game);
        frame.setSize(320, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        game.start();
    }

    // Main method to start login
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TetrisLogin().setVisible(true);
        });
    }
}
