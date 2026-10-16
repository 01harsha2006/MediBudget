package MediBudget;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Medical Expenses Management Frame with MySQL persistence,
 * family member linkage, category tracking, and aggregate expense summaries.
 */
public class MedicalExpenseFrame extends JFrame {

    private DashboardFrame dashboard;
    private User loggedInUser;
    private MedicalExpenseDAO expenseDAO = new MedicalExpenseDAO();
    private FamilyMemberDAO familyDAO = new FamilyMemberDAO();

    private JComboBox<String> patientCombo;
    private JComboBox<String> categoryCombo;
    private JTextField descriptionField;
    private JTextField amountField;
    private JTextField dateField;
    private JComboBox<String> paymentModeCombo;
    private JTextField searchField;
    private JLabel lblTotalExpenses;

    private JTable expenseTable;
    private DefaultTableModel tableModel;
    private Integer selectedExpenseId = null;

    private static final String[] CATEGORIES = {
            "Doctor Consultation",
            "Pharmacy / Medicines",
            "Lab Tests & Diagnostics",
            "Hospitalization",
            "Surgery & Procedures",
            "Dental Care",
            "Optical & Eye Care",
            "Physiotherapy & Rehab",
            "Vaccination",
            "Emergency Care",
            "Other"
    };

    private static final String[] PAYMENT_MODES = {
            "Cash", "UPI / GPay / PhonePe", "Credit / Debit Card", "Net Banking", "Insurance Direct"
    };

    public MedicalExpenseFrame() {
        this(null);
    }

    public MedicalExpenseFrame(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        this.loggedInUser = dashboard != null ? dashboard.getLoggedInUser() : null;

        setTitle("MediBudget - Medical Expense Tracker");
        setSize(980, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadPatients();
        loadExpenses();
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBackground(UITheme.BG_PAGE);

        JPanel header = UITheme.createHeaderBanner(
                "Medical Expenses",
                "Log and categorize all household healthcare expenditures",
                loggedInUser != null ? "User: " + loggedInUser.getName() : null
        );
        contentPane.add(header, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(360);
        splitPane.setDividerSize(6);
        splitPane.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        splitPane.setBackground(UITheme.BG_PAGE);

        // LEFT FORM
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);

        JLabel lblFormTitle = new JLabel("Record Expense", SwingConstants.LEFT);
        lblFormTitle.setFont(UITheme.FONT_SECTION);
        lblFormTitle.setForeground(UITheme.TEXT_DARK);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formCard.add(lblFormTitle, gbc);

        gbc.gridwidth = 1;

        patientCombo = new JComboBox<>();
        patientCombo.setFont(UITheme.FONT_BODY);
        patientCombo.setEditable(true);

        categoryCombo = UITheme.createComboBox(CATEGORIES);
        descriptionField = UITheme.createTextField();
        amountField = UITheme.createTextField();
        dateField = UITheme.createTextField();
        dateField.setText(LocalDate.now().toString());
        paymentModeCombo = UITheme.createComboBox(PAYMENT_MODES);

        addFormField(formCard, gbc, 1, "Patient Name *", patientCombo);
        addFormField(formCard, gbc, 2, "Category *", categoryCombo);
        addFormField(formCard, gbc, 3, "Description *", descriptionField);
        addFormField(formCard, gbc, 4, "Amount (₹) *", amountField);
        addFormField(formCard, gbc, 5, "Expense Date *", dateField);
        addFormField(formCard, gbc, 6, "Payment Mode", paymentModeCombo);

        // Form action buttons
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 8, 6, 8);

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("Add Expense");
        JButton btnUpdate = UITheme.createSecondaryButton("Update Selected");
        JButton btnDelete = UITheme.createDangerButton("Delete Selected");
        JButton btnClear = UITheme.createNeutralButton("Clear Form");

        btnAdd.addActionListener(e -> addExpense());
        btnUpdate.addActionListener(e -> updateExpense());
        btnDelete.addActionListener(e -> deleteExpense());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        formCard.add(btnPanel, gbc);
        splitPane.setLeftComponent(formCard);

        // RIGHT TABLE PANEL
        JPanel rightPanel = UITheme.createCardPanel();
        rightPanel.setLayout(new BorderLayout(8, 8));

        // Top Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBar.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UITheme.FONT_BODY_BOLD);
        searchField = UITheme.createTextField();
        searchField.setPreferredSize(new Dimension(160, 28));

        JButton btnSearch = UITheme.createSecondaryButton("Filter");
        JButton btnReset = UITheme.createNeutralButton("Reset");

        btnSearch.addActionListener(e -> filterTable(searchField.getText().trim()));
        btnReset.addActionListener(e -> {
            searchField.setText("");
            loadExpenses();
        });

        filterBar.add(lblSearch);
        filterBar.add(searchField);
        filterBar.add(btnSearch);
        filterBar.add(btnReset);

        rightPanel.add(filterBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Patient", "Category", "Description", "Amount", "Date", "Payment Mode"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        expenseTable = new JTable(tableModel);
        UITheme.styleTable(expenseTable);
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        expenseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = expenseTable.getSelectedRow();
                if (row != -1) {
                    populateFormFromRow(row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(expenseTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Summary Bar on Right
        JPanel summaryBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        summaryBar.setOpaque(false);
        lblTotalExpenses = new JLabel("Total Expenses: ₹0.00");
        lblTotalExpenses.setFont(UITheme.FONT_SECTION);
        lblTotalExpenses.setForeground(UITheme.TEXT_DARK);
        summaryBar.add(lblTotalExpenses);
        rightPanel.add(summaryBar, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightPanel);
        contentPane.add(splitPane, BorderLayout.CENTER);

        // Bottom Frame Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton btnBack = UITheme.createSecondaryButton("Back to Dashboard");
        btnBack.addActionListener(e -> {
            if (dashboard != null) {
                dashboard.refreshDashboardMetrics();
                dashboard.setVisible(true);
            }
            dispose();
        });
        bottomBar.add(btnBack);

        contentPane.add(bottomBar, BorderLayout.SOUTH);
        setContentPane(contentPane);
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

    private void loadPatients() {
        patientCombo.removeAllItems();
        if (loggedInUser != null) {
            patientCombo.addItem(loggedInUser.getName() + " (Self)");
            try {
                List<String> names = familyDAO.getFamilyMemberNames(loggedInUser.getId());
                for (String name : names) {
                    if (!name.equalsIgnoreCase(loggedInUser.getName())) {
                        patientCombo.addItem(name);
                    }
                }
            } catch (SQLException ignored) {}
        }
    }

    private void loadExpenses() {
        if (loggedInUser == null) return;
        tableModel.setRowCount(0);
        double total = 0.0;
        try {
            List<MedicalExpense> list = expenseDAO.getExpenses(loggedInUser.getId());
            for (MedicalExpense exp : list) {
                total += exp.getAmount();
                tableModel.addRow(new Object[]{
                        exp.getId(),
                        exp.getPatientName(),
                        exp.getCategory(),
                        exp.getDescription(),
                        UITheme.formatCurrency(exp.getAmount()),
                        exp.getExpenseDate(),
                        exp.getPaymentMode()
                });
            }
            lblTotalExpenses.setText("Total Expenses: " + UITheme.formatCurrency(total));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error while loading expenses:\n" + ex.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable(String keyword) {
        if (keyword.isEmpty()) {
            loadExpenses();
            return;
        }
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            String patient = tableModel.getValueAt(i, 1).toString().toLowerCase();
            String cat = tableModel.getValueAt(i, 2).toString().toLowerCase();
            String desc = tableModel.getValueAt(i, 3).toString().toLowerCase();
            if (!patient.contains(keyword.toLowerCase()) && !cat.contains(keyword.toLowerCase()) && !desc.contains(keyword.toLowerCase())) {
                tableModel.removeRow(i);
            }
        }
    }

    private void populateFormFromRow(int row) {
        selectedExpenseId = (Integer) memberValueSafe(expenseTable.getValueAt(row, 0));
        patientCombo.setSelectedItem(expenseTable.getValueAt(row, 1).toString());
        categoryCombo.setSelectedItem(expenseTable.getValueAt(row, 2).toString());
        descriptionField.setText(expenseTable.getValueAt(row, 3).toString());

        String amtStr = expenseTable.getValueAt(row, 4).toString().replace("₹", "").replace(",", "").trim();
        amountField.setText(amtStr);
        dateField.setText(expenseTable.getValueAt(row, 5).toString());
        paymentModeCombo.setSelectedItem(expenseTable.getValueAt(row, 6).toString());
    }

    private Object memberValueSafe(Object obj) {
        return obj;
    }

    private void clearForm() {
        selectedExpenseId = null;
        if (patientCombo.getItemCount() > 0) patientCombo.setSelectedIndex(0);
        categoryCombo.setSelectedIndex(0);
        descriptionField.setText("");
        amountField.setText("");
        dateField.setText(LocalDate.now().toString());
        paymentModeCombo.setSelectedIndex(0);
        expenseTable.clearSelection();
    }

    private void addExpense() {
        if (loggedInUser == null) return;

        Object patientObj = patientCombo.getSelectedItem();
        String patient = patientObj != null ? patientObj.toString().trim() : "";
        String desc = descriptionField.getText().trim();
        String amtText = amountField.getText().trim();
        String dateText = dateField.getText().trim();

        if (patient.isEmpty() || desc.isEmpty() || amtText.isEmpty() || dateText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amtText);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive number for amount.", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate.parse(dateText);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD (e.g. 2026-10-15).", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        MedicalExpense exp = new MedicalExpense(
                0,
                loggedInUser.getId(),
                null,
                patient,
                (String) categoryCombo.getSelectedItem(),
                desc,
                amount,
                dateText,
                (String) paymentModeCombo.getSelectedItem()
        );

        try {
            if (expenseDAO.addExpense(exp)) {
                JOptionPane.showMessageDialog(this, "Medical expense recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadExpenses();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error recording expense: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateExpense() {
        if (loggedInUser == null || selectedExpenseId == null) {
            JOptionPane.showMessageDialog(this, "Please select an expense row from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object patientObj = patientCombo.getSelectedItem();
        String patient = patientObj != null ? patientObj.toString().trim() : "";
        String desc = descriptionField.getText().trim();
        String amtText = amountField.getText().trim();
        String dateText = dateField.getText().trim();

        if (patient.isEmpty() || desc.isEmpty() || amtText.isEmpty() || dateText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amtText);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive amount.", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate.parse(dateText);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format (use YYYY-MM-DD).", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        MedicalExpense exp = new MedicalExpense(
                selectedExpenseId,
                loggedInUser.getId(),
                null,
                patient,
                (String) categoryCombo.getSelectedItem(),
                desc,
                amount,
                dateText,
                (String) paymentModeCombo.getSelectedItem()
        );

        try {
            if (expenseDAO.updateExpense(exp)) {
                JOptionPane.showMessageDialog(this, "Expense record updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadExpenses();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating expense: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteExpense() {
        if (loggedInUser == null || selectedExpenseId == null) {
            JOptionPane.showMessageDialog(this, "Please select an expense row from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this expense record?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (expenseDAO.deleteExpense(selectedExpenseId, loggedInUser.getId())) {
                    JOptionPane.showMessageDialog(this, "Expense record deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadExpenses();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting expense: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new MedicalExpenseFrame().setVisible(true);
        });
    }
}