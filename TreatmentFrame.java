package MediBudget;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Treatment Plans Management Frame with MySQL persistence,
 * hospital/doctor details, status transitions, and user isolation.
 */
public class TreatmentFrame extends JFrame {

    private DashboardFrame dashboard;
    private User loggedInUser;
    private TreatmentDAO treatmentDAO = new TreatmentDAO();
    private FamilyMemberDAO familyDAO = new FamilyMemberDAO();

    private JComboBox<String> patientCombo;
    private JTextField treatmentField;
    private JTextField hospitalField;
    private JTextField doctorField;
    private JTextField costField;
    private JComboBox<String> statusCombo;
    private JTextField startDateField;
    private JTextField notesField;
    private JTextField searchField;

    private JTable treatmentTable;
    private DefaultTableModel tableModel;
    private Integer selectedTreatmentId = null;

    private static final String[] STATUSES = {"Planned", "Ongoing", "Completed", "Cancelled"};

    public TreatmentFrame() {
        this(null);
    }

    public TreatmentFrame(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        this.loggedInUser = dashboard != null ? dashboard.getLoggedInUser() : null;

        setTitle("MediBudget - Treatment Plans");
        setSize(1000, 720);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadPatients();
        loadTreatments();
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBackground(UITheme.BG_PAGE);

        JPanel header = UITheme.createHeaderBanner(
                "Treatment Plans",
                "Plan and monitor major medical procedures, surgeries, and therapy sessions",
                loggedInUser != null ? "User: " + loggedInUser.getName() : null
        );
        contentPane.add(header, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(370);
        splitPane.setDividerSize(6);
        splitPane.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        splitPane.setBackground(UITheme.BG_PAGE);

        // LEFT FORM CARD
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 8, 5, 8);

        JLabel lblFormTitle = new JLabel("Treatment Details", SwingConstants.LEFT);
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

        treatmentField = UITheme.createTextField();
        hospitalField = UITheme.createTextField();
        doctorField = UITheme.createTextField();
        costField = UITheme.createTextField();
        statusCombo = UITheme.createComboBox(STATUSES);
        startDateField = UITheme.createTextField();
        startDateField.setText(LocalDate.now().toString());
        notesField = UITheme.createTextField();

        addFormField(formCard, gbc, 1, "Patient Name *", patientCombo);
        addFormField(formCard, gbc, 2, "Treatment Name *", treatmentField);
        addFormField(formCard, gbc, 3, "Hospital / Clinic *", hospitalField);
        addFormField(formCard, gbc, 4, "Doctor Name", doctorField);
        addFormField(formCard, gbc, 5, "Est. Cost (₹) *", costField);
        addFormField(formCard, gbc, 6, "Status", statusCombo);
        addFormField(formCard, gbc, 7, "Start Date", startDateField);
        addFormField(formCard, gbc, 8, "Notes", notesField);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 8, 4, 8);

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("Add Treatment");
        JButton btnUpdate = UITheme.createSecondaryButton("Update Selected");
        JButton btnDelete = UITheme.createDangerButton("Delete Selected");
        JButton btnClear = UITheme.createNeutralButton("Clear Form");

        btnAdd.addActionListener(e -> addTreatment());
        btnUpdate.addActionListener(e -> updateTreatment());
        btnDelete.addActionListener(e -> deleteTreatment());
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

        // Filter Bar
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
            loadTreatments();
        });

        filterBar.add(lblSearch);
        filterBar.add(searchField);
        filterBar.add(btnSearch);
        filterBar.add(btnReset);

        rightPanel.add(filterBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Patient", "Treatment", "Hospital", "Doctor", "Est. Cost", "Status", "Start Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        treatmentTable = new JTable(tableModel);
        UITheme.styleTable(treatmentTable);
        treatmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        treatmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = treatmentTable.getSelectedRow();
                if (row != -1) {
                    populateFormFromRow(row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(treatmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        splitPane.setRightComponent(rightPanel);
        contentPane.add(splitPane, BorderLayout.CENTER);

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

    private void loadTreatments() {
        if (loggedInUser == null) return;
        tableModel.setRowCount(0);
        try {
            List<Treatment> list = treatmentDAO.getTreatments(loggedInUser.getId());
            for (Treatment t : list) {
                tableModel.addRow(new Object[]{
                        t.getId(),
                        t.getPatientName(),
                        t.getTreatmentName(),
                        t.getHospital(),
                        t.getDoctorName(),
                        UITheme.formatCurrency(t.getEstimatedCost()),
                        t.getStatus(),
                        t.getStartDate()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error loading treatments:\n" + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable(String keyword) {
        if (keyword.isEmpty()) {
            loadTreatments();
            return;
        }
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            String patient = tableModel.getValueAt(i, 1).toString().toLowerCase();
            String treat = tableModel.getValueAt(i, 2).toString().toLowerCase();
            String hosp = tableModel.getValueAt(i, 3).toString().toLowerCase();
            String stat = tableModel.getValueAt(i, 6).toString().toLowerCase();
            if (!patient.contains(keyword.toLowerCase()) && !treat.contains(keyword.toLowerCase()) && !hosp.contains(keyword.toLowerCase()) && !stat.contains(keyword.toLowerCase())) {
                tableModel.removeRow(i);
            }
        }
    }

    private void populateFormFromRow(int row) {
        selectedTreatmentId = (Integer) treatmentTable.getValueAt(row, 0);
        patientCombo.setSelectedItem(treatmentTable.getValueAt(row, 1).toString());
        treatmentField.setText(treatmentTable.getValueAt(row, 2).toString());
        hospitalField.setText(treatmentTable.getValueAt(row, 3).toString());
        doctorField.setText(treatmentTable.getValueAt(row, 4).toString());

        String costStr = treatmentTable.getValueAt(row, 5).toString().replace("₹", "").replace(",", "").trim();
        costField.setText(costStr);
        statusCombo.setSelectedItem(treatmentTable.getValueAt(row, 6).toString());
        startDateField.setText(treatmentTable.getValueAt(row, 7).toString());
    }

    private void clearForm() {
        selectedTreatmentId = null;
        if (patientCombo.getItemCount() > 0) patientCombo.setSelectedIndex(0);
        treatmentField.setText("");
        hospitalField.setText("");
        doctorField.setText("");
        costField.setText("");
        statusCombo.setSelectedIndex(0);
        startDateField.setText(LocalDate.now().toString());
        notesField.setText("");
        treatmentTable.clearSelection();
    }

    private void addTreatment() {
        if (loggedInUser == null) return;
        Object patientObj = patientCombo.getSelectedItem();
        String patient = patientObj != null ? patientObj.toString().trim() : "";
        String treatment = treatmentField.getText().trim();
        String hospital = hospitalField.getText().trim();
        String costText = costField.getText().trim();
        String sDate = startDateField.getText().trim();

        if (patient.isEmpty() || treatment.isEmpty() || hospital.isEmpty() || costText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double cost;
        try {
            cost = Double.parseDouble(costText);
            if (cost < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive estimated cost.", "Invalid Cost", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!sDate.isEmpty()) {
            try {
                LocalDate.parse(sDate);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        Treatment t = new Treatment(
                0,
                loggedInUser.getId(),
                null,
                patient,
                treatment,
                hospital,
                doctorField.getText().trim(),
                cost,
                (String) statusCombo.getSelectedItem(),
                sDate,
                notesField.getText().trim()
        );

        try {
            if (treatmentDAO.addTreatment(t)) {
                JOptionPane.showMessageDialog(this, "Treatment plan added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadTreatments();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding treatment: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTreatment() {
        if (loggedInUser == null || selectedTreatmentId == null) {
            JOptionPane.showMessageDialog(this, "Please select a treatment from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object patientObj = patientCombo.getSelectedItem();
        String patient = patientObj != null ? patientObj.toString().trim() : "";
        String treatment = treatmentField.getText().trim();
        String hospital = hospitalField.getText().trim();
        String costText = costField.getText().trim();
        String sDate = startDateField.getText().trim();

        if (patient.isEmpty() || treatment.isEmpty() || hospital.isEmpty() || costText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double cost;
        try {
            cost = Double.parseDouble(costText);
            if (cost < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid cost.", "Invalid Cost", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!sDate.isEmpty()) {
            try {
                LocalDate.parse(sDate);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format (use YYYY-MM-DD).", "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        Treatment t = new Treatment(
                selectedTreatmentId,
                loggedInUser.getId(),
                null,
                patient,
                treatment,
                hospital,
                doctorField.getText().trim(),
                cost,
                (String) statusCombo.getSelectedItem(),
                sDate,
                notesField.getText().trim()
        );

        try {
            if (treatmentDAO.updateTreatment(t)) {
                JOptionPane.showMessageDialog(this, "Treatment plan updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadTreatments();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating treatment: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTreatment() {
        if (loggedInUser == null || selectedTreatmentId == null) {
            JOptionPane.showMessageDialog(this, "Please select a treatment from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this treatment plan?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (treatmentDAO.deleteTreatment(selectedTreatmentId, loggedInUser.getId())) {
                    JOptionPane.showMessageDialog(this, "Treatment plan deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadTreatments();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting treatment: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new TreatmentFrame().setVisible(true);
        });
    }
}