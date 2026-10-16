package MediBudget;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Polished Registration Frame with input validation, password confirmation,
 * duplicate username prevention, and cryptographic password hashing.
 */
public class RegisterFrame extends JFrame {

    private JTextField nameField;
    private JTextField departmentField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    private UserDAO userDAO = new UserDAO();

    public RegisterFrame() {
        setTitle("MediBudget - Create Account");
        setSize(540, 580);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(UITheme.BG_PAGE);

        JPanel header = UITheme.createHeaderBanner(
                "Create Your Account",
                "Join MediBudget to protect household healthcare finances",
                null
        );
        contentPane.add(header, BorderLayout.NORTH);

        JPanel cardWrapper = new JPanel(new GridBagLayout());
        cardWrapper.setBackground(UITheme.BG_PAGE);

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(460, 420));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 12, 5, 12);

        JLabel lblTitle = new JLabel("Registration Form", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_SECTION);
        lblTitle.setForeground(UITheme.TEXT_DARK);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 12, 10, 12);
        card.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(4, 12, 4, 12);

        nameField = UITheme.createTextField();
        departmentField = UITheme.createTextField();
        usernameField = UITheme.createTextField();
        passwordField = UITheme.createPasswordField();
        confirmPasswordField = UITheme.createPasswordField();

        addFormField(card, gbc, 1, "Full Name *", nameField);
        addFormField(card, gbc, 2, "Department / Occupation", departmentField);
        addFormField(card, gbc, 3, "Username *", usernameField);
        addFormField(card, gbc, 4, "Password *", passwordField);
        addFormField(card, gbc, 5, "Confirm Password *", confirmPasswordField);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 12, 6, 12);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        btnPanel.setOpaque(false);

        JButton btnRegister = UITheme.createPrimaryButton("Create Account");
        JButton btnBack = UITheme.createSecondaryButton("Back to Login");

        btnRegister.addActionListener(e -> register());
        btnBack.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        btnPanel.add(btnRegister);
        btnPanel.add(btnBack);
        card.add(btnPanel, gbc);

        cardWrapper.add(card);
        contentPane.add(cardWrapper, BorderLayout.CENTER);

        setContentPane(contentPane);
        getRootPane().setDefaultButton(btnRegister);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.TEXT_DARK);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    private void register() {
        String name = nameField.getText().trim();
        String department = departmentField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (name.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields (marked with *).",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (username.length() < 3) {
            JOptionPane.showMessageDialog(this,
                    "Username must be at least 3 characters long.",
                    "Invalid Username",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 4 characters long for security.",
                    "Weak Password",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match. Please re-enter identical passwords.",
                    "Password Mismatch",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            User newUser = userDAO.registerUser(name, department, username, password);
            if (newUser != null) {
                JOptionPane.showMessageDialog(this,
                        "Registration successful!\n\nWelcome, " + name + "!\nYou can now log in.",
                        "Account Created",
                        JOptionPane.INFORMATION_MESSAGE);

                LoginFrame loginFrame = new LoginFrame(newUser);
                loginFrame.setVisible(true);
                dispose();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Registration Notice",
                    JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            int opt = JOptionPane.showConfirmDialog(this,
                    "Database error while registering:\n" + ex.getMessage()
                            + "\n\nWould you like to configure your MySQL database connection?",
                    "Database Error",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE);
            if (opt == JOptionPane.YES_OPTION) {
                new DatabaseConfigDialog(this).setVisible(true);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new RegisterFrame().setVisible(true);
        });
    }
}
