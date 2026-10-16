package MediBudget;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Modal dialog for configuring MySQL database credentials securely,
 * testing connection liveliness, and auto-initializing tables.
 */
public class DatabaseConfigDialog extends JDialog {

    private JTextField hostField;
    private JTextField portField;
    private JTextField dbField;
    private JTextField userField;
    private JPasswordField passField;
    private JLabel statusLabel;
    private boolean configSuccessful = false;

    public DatabaseConfigDialog(Frame owner) {
        super(owner, "MediBudget - Database Configuration", true);
        setSize(480, 480);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(UITheme.BG_PAGE);

        JPanel header = UITheme.createHeaderBanner(
                "Database Setup",
                "Configure local MySQL connection parameters",
                null
        );
        contentPane.add(header, BorderLayout.NORTH);

        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        hostField = UITheme.createTextField();
        hostField.setText(DBConnection.getHost());

        portField = UITheme.createTextField();
        portField.setText(DBConnection.getPort());

        dbField = UITheme.createTextField();
        dbField.setText(DBConnection.getDbName());

        userField = UITheme.createTextField();
        userField.setText(DBConnection.getUser());

        passField = UITheme.createPasswordField();
        passField.setText(DBConnection.getPassword());

        addField(formCard, gbc, 0, "MySQL Host:", hostField);
        addField(formCard, gbc, 1, "Port:", portField);
        addField(formCard, gbc, 2, "Database Name:", dbField);
        addField(formCard, gbc, 3, "Username:", userField);
        addField(formCard, gbc, 4, "Password:", passField);

        statusLabel = new JLabel("Status: Please verify credentials", SwingConstants.CENTER);
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.TEXT_MUTED);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 10, 4, 10);
        formCard.add(statusLabel, gbc);

        contentPane.add(formCard, BorderLayout.CENTER);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonBar.setBackground(Color.WHITE);
        buttonBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton btnTest = UITheme.createSecondaryButton("Test Connection");
        JButton btnSave = UITheme.createPrimaryButton("Save & Initialize DB");
        JButton btnCancel = UITheme.createNeutralButton("Cancel");

        btnTest.addActionListener(e -> testConnection());
        btnSave.addActionListener(e -> saveAndInitialize());
        btnCancel.addActionListener(e -> dispose());

        buttonBar.add(btnTest);
        buttonBar.add(btnSave);
        buttonBar.add(btnCancel);

        contentPane.add(buttonBar, BorderLayout.SOUTH);
        setContentPane(contentPane);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.TEXT_DARK);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    private void testConnection() {
        String host = hostField.getText().trim();
        String port = portField.getText().trim();
        String dbName = dbField.getText().trim();
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        statusLabel.setText("Connecting to MySQL...");
        statusLabel.setForeground(UITheme.WARNING);

        SwingUtilities.invokeLater(() -> {
            try {
                boolean ok = DBConnection.testConnection(host, port, dbName, user, pass);
                if (ok) {
                    statusLabel.setText("Connection successful!");
                    statusLabel.setForeground(UITheme.SUCCESS);
                } else {
                    statusLabel.setText("Connection failed. Check parameters.");
                    statusLabel.setForeground(UITheme.DANGER);
                }
            } catch (SQLException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
                statusLabel.setForeground(UITheme.DANGER);
            }
        });
    }

    private void saveAndInitialize() {
        String host = hostField.getText().trim();
        String port = portField.getText().trim();
        String dbName = dbField.getText().trim();
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        try {
            DBConnection.saveConfiguration(host, port, dbName, user, pass);
            DBConnection.initDatabase();
            configSuccessful = true;
            JOptionPane.showMessageDialog(this,
                    "Database configuration saved and tables initialized successfully!",
                    "MediBudget Database Ready",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            statusLabel.setText("✗ Save / Init Error: " + ex.getMessage());
            statusLabel.setForeground(UITheme.DANGER);
            JOptionPane.showMessageDialog(this,
                    "Failed to initialize database:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfigSuccessful() {
        return configSuccessful;
    }
}
