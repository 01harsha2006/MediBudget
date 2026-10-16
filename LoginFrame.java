package MediBudget;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Modern, polished Login Frame with healthcare-finance purple styling,
 * database connectivity check, and seamless registration workflow.
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private UserDAO userDAO = new UserDAO();

    // Default constructor
    public LoginFrame() {
        this(null);
    }

    // Constructor receiving newly registered user (for pre-filling)
    public LoginFrame(User registeredUser) {
        setTitle("MediBudget - Secure Login");
        setSize(520, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Ensure database connection / tables exist
        checkDatabaseReadiness();

        initUI(registeredUser);
    }

    private void checkDatabaseReadiness() {
        try {
            DBConnection.initDatabase();
        } catch (Exception e) {
            System.err.println("[MediBudget] Database initial connection check: " + e.getMessage());
            // We do not block UI here; if user clicks login/register they can configure credentials.
        }
    }

    private void initUI(User prefillUser) {
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(UITheme.BG_PAGE);

        // Header Banner
        JPanel header = UITheme.createHeaderBanner(
                "MediBudget",
                "Medical Expense Planner for Household Financial Protection",
                null
        );
        contentPane.add(header, BorderLayout.NORTH);

        // Form Card Panel
        JPanel cardWrapper = new JPanel(new GridBagLayout());
        cardWrapper.setBackground(UITheme.BG_PAGE);

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(420, 290));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 12, 8, 12);

        JLabel lblLoginTitle = new JLabel("Sign In to Your Account", SwingConstants.CENTER);
        lblLoginTitle.setFont(UITheme.FONT_SECTION);
        lblLoginTitle.setForeground(UITheme.TEXT_DARK);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 12, 16, 12);
        card.add(lblLoginTitle, gbc);

        gbc.insets = new Insets(6, 12, 4, 12);
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(UITheme.FONT_BODY_BOLD);
        lblUser.setForeground(UITheme.TEXT_DARK);
        card.add(lblUser, gbc);

        gbc.gridy = 2;
        usernameField = UITheme.createTextField();
        if (prefillUser != null) {
            usernameField.setText(prefillUser.getUsername());
        }
        card.add(usernameField, gbc);

        gbc.gridy = 3;
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UITheme.FONT_BODY_BOLD);
        lblPass.setForeground(UITheme.TEXT_DARK);
        card.add(lblPass, gbc);

        gbc.gridy = 4;
        passwordField = UITheme.createPasswordField();
        card.add(passwordField, gbc);

        // Buttons
        gbc.gridy = 5;
        gbc.insets = new Insets(16, 12, 8, 12);
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnLogin = UITheme.createPrimaryButton("Login");
        JButton btnRegister = UITheme.createSecondaryButton("Register");

        btnLogin.addActionListener(e -> login());
        btnRegister.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });

        btnPanel.add(btnLogin);
        btnPanel.add(btnRegister);
        card.add(btnPanel, gbc);

        cardWrapper.add(card);
        contentPane.add(cardWrapper, BorderLayout.CENTER);

        // Footer Bar with DB Settings
        JPanel footerBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        footerBar.setBackground(UITheme.BG_PAGE);
        JButton btnDbConfig = UITheme.createSecondaryButton("Database Setup & Status");
        btnDbConfig.setFont(UITheme.FONT_SMALL);
        btnDbConfig.addActionListener(e -> openDbConfigDialog());
        footerBar.add(btnDbConfig);

        contentPane.add(footerBar, BorderLayout.SOUTH);

        setContentPane(contentPane);
        getRootPane().setDefaultButton(btnLogin);
    }

    private void openDbConfigDialog() {
        DatabaseConfigDialog dlg = new DatabaseConfigDialog(this);
        dlg.setVisible(true);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = userDAO.loginUser(username, password);
            if (user != null) {
                JOptionPane.showMessageDialog(this,
                        "Login successful!\nWelcome back, " + user.getName() + "!",
                        "MediBudget - Authenticated",
                        JOptionPane.INFORMATION_MESSAGE);

                DashboardFrame dashboard = new DashboardFrame(user);
                dashboard.setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password. Please verify credentials or register.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            int opt = JOptionPane.showConfirmDialog(this,
                    "Database connection error:\n" + ex.getMessage() + "\n\nWould you like to configure MySQL database credentials now?",
                    "Database Error",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE);
            if (opt == JOptionPane.YES_OPTION) {
                openDbConfigDialog();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}
