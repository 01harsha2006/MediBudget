package MediBudget;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Health Insurance Policies and Claims Management Frame with MySQL persistence,
 * tabbed views, policy-claim linkages, and status workflows.
 */
public class InsuranceFrame extends JFrame {

    private DashboardFrame dashboard;
    private User loggedInUser;
    private InsuranceDAO insuranceDAO = new InsuranceDAO();
    private FamilyMemberDAO familyDAO = new FamilyMemberDAO();

    // Policy Tab Components
    private JTextField providerField;
    private JTextField policyNumberField;
    private JComboBox<String> policyTypeCombo;
    private JTextField coverageField;
    private JTextField premiumField;
    private JTextField expiryDateField;
    private JTextField policyNotesField;
    private JTable policyTable;
    private DefaultTableModel policyTableModel;
    private Integer selectedPolicyId = null;

    // Claims Tab Components
    private JComboBox<String> claimPatientCombo;
    private JComboBox<String> claimPolicyCombo;
    private JTextField claimAmountField;
    private JTextField settledAmountField;
    private JComboBox<String> claimStatusCombo;
    private JTextField claimDateField;
    private JTextField claimRemarksField;
    private JTable claimTable;
    private DefaultTableModel claimTableModel;
    private Integer selectedClaimId = null;

    private static final String[] POLICY_TYPES = {
            "Family Floater", "Individual Health", "Critical Illness", "Senior Citizen Red Carpet", "Accidental Benefit"
    };

    private static final String[] CLAIM_STATUSES = {
            "Submitted", "Under Review", "Approved", "Rejected", "Settled"
    };

    public InsuranceFrame() {
        this(null);
    }

    public InsuranceFrame(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        this.loggedInUser = dashboard != null ? dashboard.getLoggedInUser() : null;

        setTitle("MediBudget - Insurance Policies & Claims");
        setSize(1020, 720);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadPolicies();
        loadClaims();
        loadClaimDropdowns();
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBackground(UITheme.BG_PAGE);

        JPanel header = UITheme.createHeaderBanner(
                "Health Insurance & Claims",
                "Track insurance policies, coverage caps, premium renewals, and claim reimbursements",
                loggedInUser != null ? "User: " + loggedInUser.getName() : null
        );
        contentPane.add(header, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BODY_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Policies", createPolicyPanel());
        tabbedPane.addTab("Insurance Claims", createClaimPanel());

        contentPane.add(tabbedPane, BorderLayout.CENTER);

        // Bottom Bar
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

    // ==========================================
    // POLICIES PANEL
    // ==========================================
    private JPanel createPolicyPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_PAGE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(360);
        splitPane.setDividerSize(6);
        splitPane.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        splitPane.setBackground(UITheme.BG_PAGE);

        // Left Form
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 8, 5, 8);

        JLabel lblTitle = new JLabel("Policy Information", SwingConstants.LEFT);
        lblTitle.setFont(UITheme.FONT_SECTION);
        lblTitle.setForeground(UITheme.TEXT_DARK);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formCard.add(lblTitle, gbc);

        gbc.gridwidth = 1;

        providerField = UITheme.createTextField();
        policyNumberField = UITheme.createTextField();
        policyTypeCombo = UITheme.createComboBox(POLICY_TYPES);
        coverageField = UITheme.createTextField();
        premiumField = UITheme.createTextField();
        expiryDateField = UITheme.createTextField();
        expiryDateField.setText(LocalDate.now().plusYears(1).toString());
        policyNotesField = UITheme.createTextField();

        addFormField(formCard, gbc, 1, "Insurance Provider *", providerField);
        addFormField(formCard, gbc, 2, "Policy Number *", policyNumberField);
        addFormField(formCard, gbc, 3, "Policy Type", policyTypeCombo);
        addFormField(formCard, gbc, 4, "Coverage (₹) *", coverageField);
        addFormField(formCard, gbc, 5, "Annual Premium (₹)", premiumField);
        addFormField(formCard, gbc, 6, "Expiry Date", expiryDateField);
        addFormField(formCard, gbc, 7, "Notes", policyNotesField);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 8, 4, 8);

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("Add Policy");
        JButton btnUpdate = UITheme.createSecondaryButton("Update Selected");
        JButton btnDelete = UITheme.createDangerButton("Delete Selected");
        JButton btnClear = UITheme.createNeutralButton("Clear");

        btnAdd.addActionListener(e -> addPolicy());
        btnUpdate.addActionListener(e -> updatePolicy());
        btnDelete.addActionListener(e -> deletePolicy());
        btnClear.addActionListener(e -> clearPolicyForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        formCard.add(btnPanel, gbc);

        splitPane.setLeftComponent(formCard);

        // Right Table
        JPanel rightCard = UITheme.createCardPanel();
        rightCard.setLayout(new BorderLayout(8, 8));

        String[] cols = {"ID", "Provider", "Policy #", "Type", "Coverage", "Premium", "Expiry"};
        policyTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        policyTable = new JTable(policyTableModel);
        UITheme.styleTable(policyTable);
        policyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        policyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int r = policyTable.getSelectedRow();
                if (r != -1) populatePolicyForm(r);
            }
        });

        rightCard.add(new JScrollPane(policyTable), BorderLayout.CENTER);
        splitPane.setRightComponent(rightCard);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // CLAIMS PANEL
    // ==========================================
    private JPanel createClaimPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_PAGE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(360);
        splitPane.setDividerSize(6);
        splitPane.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        splitPane.setBackground(UITheme.BG_PAGE);

        // Left Form
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 8, 5, 8);

        JLabel lblTitle = new JLabel("Claim Details", SwingConstants.LEFT);
        lblTitle.setFont(UITheme.FONT_SECTION);
        lblTitle.setForeground(UITheme.TEXT_DARK);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formCard.add(lblTitle, gbc);

        gbc.gridwidth = 1;

        claimPatientCombo = new JComboBox<>();
        claimPatientCombo.setFont(UITheme.FONT_BODY);
        claimPatientCombo.setEditable(true);

        claimPolicyCombo = new JComboBox<>();
        claimPolicyCombo.setFont(UITheme.FONT_BODY);

        claimAmountField = UITheme.createTextField();
        settledAmountField = UITheme.createTextField();
        settledAmountField.setText("0.00");
        claimStatusCombo = UITheme.createComboBox(CLAIM_STATUSES);
        claimDateField = UITheme.createTextField();
        claimDateField.setText(LocalDate.now().toString());
        claimRemarksField = UITheme.createTextField();

        addFormField(formCard, gbc, 1, "Patient Name *", claimPatientCombo);
        addFormField(formCard, gbc, 2, "Linked Policy", claimPolicyCombo);
        addFormField(formCard, gbc, 3, "Claim Amount (₹) *", claimAmountField);
        addFormField(formCard, gbc, 4, "Settled Amount (₹)", settledAmountField);
        addFormField(formCard, gbc, 5, "Claim Status", claimStatusCombo);
        addFormField(formCard, gbc, 6, "Claim Date *", claimDateField);
        addFormField(formCard, gbc, 7, "Remarks / Reference", claimRemarksField);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 8, 4, 8);

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("Submit Claim");
        JButton btnUpdate = UITheme.createSecondaryButton("Update Selected");
        JButton btnDelete = UITheme.createDangerButton("Delete Selected");
        JButton btnClear = UITheme.createNeutralButton("Clear");

        btnAdd.addActionListener(e -> addClaim());
        btnUpdate.addActionListener(e -> updateClaim());
        btnDelete.addActionListener(e -> deleteClaim());
        btnClear.addActionListener(e -> clearClaimForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        formCard.add(btnPanel, gbc);

        splitPane.setLeftComponent(formCard);

        // Right Table
        JPanel rightCard = UITheme.createCardPanel();
        rightCard.setLayout(new BorderLayout(8, 8));

        String[] cols = {"ID", "Patient", "Claim Amt", "Settled Amt", "Status", "Claim Date", "Remarks"};
        claimTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        claimTable = new JTable(claimTableModel);
        UITheme.styleTable(claimTable);
        claimTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        claimTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int r = claimTable.getSelectedRow();
                if (r != -1) populateClaimForm(r);
            }
        });

        rightCard.add(new JScrollPane(claimTable), BorderLayout.CENTER);
        splitPane.setRightComponent(rightCard);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
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

    private void loadPolicies() {
        if (loggedInUser == null) return;
        policyTableModel.setRowCount(0);
        try {
            List<InsurancePolicy> list = insuranceDAO.getPolicies(loggedInUser.getId());
            for (InsurancePolicy p : list) {
                policyTableModel.addRow(new Object[]{
                        p.getId(),
                        p.getProvider(),
                        p.getPolicyNumber(),
                        p.getPolicyType(),
                        UITheme.formatCurrency(p.getCoverageAmount()),
                        UITheme.formatCurrency(p.getPremiumAmount()),
                        p.getExpiryDate()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading policies: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadClaims() {
        if (loggedInUser == null) return;
        claimTableModel.setRowCount(0);
        try {
            List<InsuranceClaim> list = insuranceDAO.getClaims(loggedInUser.getId());
            for (InsuranceClaim c : list) {
                claimTableModel.addRow(new Object[]{
                        c.getId(),
                        c.getPatientName(),
                        UITheme.formatCurrency(c.getClaimAmount()),
                        UITheme.formatCurrency(c.getSettledAmount()),
                        c.getStatus(),
                        c.getClaimDate(),
                        c.getRemarks()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading claims: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadClaimDropdowns() {
        claimPatientCombo.removeAllItems();
        claimPolicyCombo.removeAllItems();
        claimPolicyCombo.addItem("None / Standalone");

        if (loggedInUser != null) {
            claimPatientCombo.addItem(loggedInUser.getName() + " (Self)");
            try {
                List<String> names = familyDAO.getFamilyMemberNames(loggedInUser.getId());
                for (String n : names) {
                    if (!n.equalsIgnoreCase(loggedInUser.getName())) {
                        claimPatientCombo.addItem(n);
                    }
                }
                List<InsurancePolicy> pols = insuranceDAO.getPolicies(loggedInUser.getId());
                for (InsurancePolicy p : pols) {
                    claimPolicyCombo.addItem(p.getProvider() + " (" + p.getPolicyNumber() + ")");
                }
            } catch (SQLException ignored) {}
        }
    }

    private void populatePolicyForm(int r) {
        selectedPolicyId = (Integer) policyTable.getValueAt(r, 0);
        providerField.setText(policyTable.getValueAt(r, 1).toString());
        policyNumberField.setText(policyTable.getValueAt(r, 2).toString());
        policyTypeCombo.setSelectedItem(policyTable.getValueAt(r, 3).toString());

        String cov = policyTable.getValueAt(r, 4).toString().replace("₹", "").replace(",", "").trim();
        coverageField.setText(cov);

        String prem = policyTable.getValueAt(r, 5).toString().replace("₹", "").replace(",", "").trim();
        premiumField.setText(prem);

        expiryDateField.setText(policyTable.getValueAt(r, 6).toString());
    }

    private void clearPolicyForm() {
        selectedPolicyId = null;
        providerField.setText("");
        policyNumberField.setText("");
        policyTypeCombo.setSelectedIndex(0);
        coverageField.setText("");
        premiumField.setText("");
        expiryDateField.setText(LocalDate.now().plusYears(1).toString());
        policyNotesField.setText("");
        policyTable.clearSelection();
    }

    private void addPolicy() {
        if (loggedInUser == null) return;
        String provider = providerField.getText().trim();
        String policyNum = policyNumberField.getText().trim();
        String covText = coverageField.getText().trim();

        if (provider.isEmpty() || policyNum.isEmpty() || covText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Provider, Policy Number, and Coverage Amount.", "Missing Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double cov, prem = 0.0;
        try {
            cov = Double.parseDouble(covText);
            if (!premiumField.getText().trim().isEmpty()) {
                prem = Double.parseDouble(premiumField.getText().trim());
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric amounts.", "Invalid Numbers", JOptionPane.WARNING_MESSAGE);
            return;
        }

        InsurancePolicy policy = new InsurancePolicy(
                0,
                loggedInUser.getId(),
                provider,
                policyNum,
                (String) policyTypeCombo.getSelectedItem(),
                cov,
                prem,
                expiryDateField.getText().trim(),
                policyNotesField.getText().trim()
        );

        try {
            if (insuranceDAO.addPolicy(policy)) {
                JOptionPane.showMessageDialog(this, "Insurance policy registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearPolicyForm();
                loadPolicies();
                loadClaimDropdowns();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding policy: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePolicy() {
        if (loggedInUser == null || selectedPolicyId == null) {
            JOptionPane.showMessageDialog(this, "Select a policy row from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String provider = providerField.getText().trim();
        String policyNum = policyNumberField.getText().trim();
        String covText = coverageField.getText().trim();

        if (provider.isEmpty() || policyNum.isEmpty() || covText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill required fields.", "Missing Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double cov, prem = 0.0;
        try {
            cov = Double.parseDouble(covText);
            if (!premiumField.getText().trim().isEmpty()) {
                prem = Double.parseDouble(premiumField.getText().trim());
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric amounts.", "Invalid Numbers", JOptionPane.WARNING_MESSAGE);
            return;
        }

        InsurancePolicy policy = new InsurancePolicy(
                selectedPolicyId,
                loggedInUser.getId(),
                provider,
                policyNum,
                (String) policyTypeCombo.getSelectedItem(),
                cov,
                prem,
                expiryDateField.getText().trim(),
                policyNotesField.getText().trim()
        );

        try {
            if (insuranceDAO.updatePolicy(policy)) {
                JOptionPane.showMessageDialog(this, "Policy updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearPolicyForm();
                loadPolicies();
                loadClaimDropdowns();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating policy: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePolicy() {
        if (loggedInUser == null || selectedPolicyId == null) {
            JOptionPane.showMessageDialog(this, "Select a policy from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this policy record?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (insuranceDAO.deletePolicy(selectedPolicyId, loggedInUser.getId())) {
                    JOptionPane.showMessageDialog(this, "Policy deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearPolicyForm();
                    loadPolicies();
                    loadClaimDropdowns();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting policy: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void populateClaimForm(int r) {
        selectedClaimId = (Integer) claimTable.getValueAt(r, 0);
        claimPatientCombo.setSelectedItem(claimTable.getValueAt(r, 1).toString());

        String clmAmt = claimTable.getValueAt(r, 2).toString().replace("₹", "").replace(",", "").trim();
        claimAmountField.setText(clmAmt);

        String setAmt = claimTable.getValueAt(r, 3).toString().replace("₹", "").replace(",", "").trim();
        settledAmountField.setText(setAmt);

        claimStatusCombo.setSelectedItem(claimTable.getValueAt(r, 4).toString());
        claimDateField.setText(claimTable.getValueAt(r, 5).toString());
        claimRemarksField.setText(claimTable.getValueAt(r, 6).toString());
    }

    private void clearClaimForm() {
        selectedClaimId = null;
        if (claimPatientCombo.getItemCount() > 0) claimPatientCombo.setSelectedIndex(0);
        claimAmountField.setText("");
        settledAmountField.setText("0.00");
        claimStatusCombo.setSelectedIndex(0);
        claimDateField.setText(LocalDate.now().toString());
        claimRemarksField.setText("");
        claimTable.clearSelection();
    }

    private void addClaim() {
        if (loggedInUser == null) return;
        Object pObj = claimPatientCombo.getSelectedItem();
        String patient = pObj != null ? pObj.toString().trim() : "";
        String amtText = claimAmountField.getText().trim();
        String cDate = claimDateField.getText().trim();

        if (patient.isEmpty() || amtText.isEmpty() || cDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Patient Name, Claim Amount, and Date.", "Missing Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double claimAmt, settledAmt = 0.0;
        try {
            claimAmt = Double.parseDouble(amtText);
            if (!settledAmountField.getText().trim().isEmpty()) {
                settledAmt = Double.parseDouble(settledAmountField.getText().trim());
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid amounts.", "Invalid Numbers", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate.parse(cDate);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        InsuranceClaim claim = new InsuranceClaim(
                0,
                loggedInUser.getId(),
                null,
                null,
                patient,
                claimAmt,
                settledAmt,
                (String) claimStatusCombo.getSelectedItem(),
                cDate,
                claimRemarksField.getText().trim()
        );

        try {
            if (insuranceDAO.addClaim(claim)) {
                JOptionPane.showMessageDialog(this, "Insurance claim submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearClaimForm();
                loadClaims();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error submitting claim: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateClaim() {
        if (loggedInUser == null || selectedClaimId == null) {
            JOptionPane.showMessageDialog(this, "Select a claim from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object pObj = claimPatientCombo.getSelectedItem();
        String patient = pObj != null ? pObj.toString().trim() : "";
        String amtText = claimAmountField.getText().trim();
        String cDate = claimDateField.getText().trim();

        if (patient.isEmpty() || amtText.isEmpty() || cDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill required fields.", "Missing Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double claimAmt, settledAmt = 0.0;
        try {
            claimAmt = Double.parseDouble(amtText);
            if (!settledAmountField.getText().trim().isEmpty()) {
                settledAmt = Double.parseDouble(settledAmountField.getText().trim());
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid amounts.", "Invalid Numbers", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate.parse(cDate);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format (use YYYY-MM-DD).", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        InsuranceClaim claim = new InsuranceClaim(
                selectedClaimId,
                loggedInUser.getId(),
                null,
                null,
                patient,
                claimAmt,
                settledAmt,
                (String) claimStatusCombo.getSelectedItem(),
                cDate,
                claimRemarksField.getText().trim()
        );

        try {
            if (insuranceDAO.updateClaim(claim)) {
                JOptionPane.showMessageDialog(this, "Claim updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearClaimForm();
                loadClaims();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating claim: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteClaim() {
        if (loggedInUser == null || selectedClaimId == null) {
            JOptionPane.showMessageDialog(this, "Select a claim from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this claim record?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (insuranceDAO.deleteClaim(selectedClaimId, loggedInUser.getId())) {
                    JOptionPane.showMessageDialog(this, "Claim deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearClaimForm();
                    loadClaims();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting claim: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new InsuranceFrame().setVisible(true);
        });
    }
}