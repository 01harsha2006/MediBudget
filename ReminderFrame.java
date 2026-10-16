package MediBudget;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Health Reminders Management Frame with MySQL persistence,
 * quick completion toggle, patient linkages, and status filtering.
 */
public class ReminderFrame extends JFrame {

    private DashboardFrame dashboard;
    private User loggedInUser;
    private ReminderDAO reminderDAO = new ReminderDAO();
    private FamilyMemberDAO familyDAO = new FamilyMemberDAO();

    private JComboBox<String> patientCombo;
    private JComboBox<String> typeCombo;
    private JTextField descriptionField;
    private JTextField dateField;
    private JComboBox<String> statusCombo;
    private JTextField searchField;

    private JTable reminderTable;
    private DefaultTableModel tableModel;
    private Integer selectedReminderId = null;

    private static final String[] REMINDER_TYPES = {
            "Medication Refill",
            "Doctor Appointment",
            "Diagnostic Lab Test",
            "Insurance Premium Renewal",
            "Vaccination Dose",
            "Routine Health Checkup",
            "Follow-up Consultation"
    };

    private static final String[] STATUSES = {"Pending", "Completed", "Dismissed"};

    public ReminderFrame() {
        this(null);
    }

    public ReminderFrame(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        this.loggedInUser = dashboard != null ? dashboard.getLoggedInUser() : null;

        setTitle("MediBudget - Healthcare Reminders");
        setSize(980, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadPatients();
        loadReminders();
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBackground(UITheme.BG_PAGE);

        JPanel header = UITheme.createHeaderBanner(
                "Healthcare Reminders",
                "Ensure timely medication refills, follow-ups, tests, and insurance renewals",
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

        JLabel lblFormTitle = new JLabel("Reminder Details", SwingConstants.LEFT);
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

        typeCombo = UITheme.createComboBox(REMINDER_TYPES);
        descriptionField = UITheme.createTextField();
        dateField = UITheme.createTextField();
        dateField.setText(LocalDate.now().plusDays(3).toString());
        statusCombo = UITheme.createComboBox(STATUSES);

        addFormField(formCard, gbc, 1, "Patient Name *", patientCombo);
        addFormField(formCard, gbc, 2, "Reminder Type", typeCombo);
        addFormField(formCard, gbc, 3, "Description *", descriptionField);
        addFormField(formCard, gbc, 4, "Due Date *", dateField);
        addFormField(formCard, gbc, 5, "Status", statusCombo);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 8, 4, 8);

        JPanel btnPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        btnPanel.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("Add Reminder");
        JButton btnUpdate = UITheme.createSecondaryButton("Update Selected");
        JButton btnComplete = UITheme.createSuccessButton("Mark Done");
        JButton btnDelete = UITheme.createDangerButton("Delete Selected");
        JButton btnClear = UITheme.createNeutralButton("Clear Form");

        btnAdd.addActionListener(e -> addReminder());
        btnUpdate.addActionListener(e -> updateReminder());
        btnComplete.addActionListener(e -> markComplete());
        btnDelete.addActionListener(e -> deleteReminder());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnComplete);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        formCard.add(btnPanel, gbc);
        splitPane.setLeftComponent(formCard);

        // RIGHT TABLE
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
            loadReminders();
        });

        filterBar.add(lblSearch);
        filterBar.add(searchField);
        filterBar.add(btnSearch);
        filterBar.add(btnReset);

        rightPanel.add(filterBar, BorderLayout.NORTH);

        String[] columns = {"ID", "Patient", "Type", "Description", "Due Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        reminderTable = new JTable(tableModel);
        UITheme.styleTable(reminderTable);
        reminderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        reminderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = reminderTable.getSelectedRow();
                if (row != -1) populateFormFromRow(row);
            }
        });

        rightPanel.add(new JScrollPane(reminderTable), BorderLayout.CENTER);
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

    private void loadReminders() {
        if (loggedInUser == null) return;
        tableModel.setRowCount(0);
        try {
            List<Reminder> list = reminderDAO.getReminders(loggedInUser.getId());
            for (Reminder r : list) {
                tableModel.addRow(new Object[]{
                        r.getId(),
                        r.getPatientName(),
                        r.getReminderType(),
                        r.getDescription(),
                        r.getDate(),
                        r.getStatus()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading reminders: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable(String keyword) {
        if (keyword.isEmpty()) {
            loadReminders();
            return;
        }
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            String patient = tableModel.getValueAt(i, 1).toString().toLowerCase();
            String type = tableModel.getValueAt(i, 2).toString().toLowerCase();
            String desc = tableModel.getValueAt(i, 3).toString().toLowerCase();
            String stat = tableModel.getValueAt(i, 5).toString().toLowerCase();
            if (!patient.contains(keyword.toLowerCase()) && !type.contains(keyword.toLowerCase())
                    && !desc.contains(keyword.toLowerCase()) && !stat.contains(keyword.toLowerCase())) {
                tableModel.removeRow(i);
            }
        }
    }

    private void populateFormFromRow(int row) {
        selectedReminderId = (Integer) reminderTable.getValueAt(row, 0);
        patientCombo.setSelectedItem(reminderTable.getValueAt(row, 1).toString());
        typeCombo.setSelectedItem(reminderTable.getValueAt(row, 2).toString());
        descriptionField.setText(reminderTable.getValueAt(row, 3).toString());
        dateField.setText(reminderTable.getValueAt(row, 4).toString());
        statusCombo.setSelectedItem(reminderTable.getValueAt(row, 5).toString());
    }

    private void clearForm() {
        selectedReminderId = null;
        if (patientCombo.getItemCount() > 0) patientCombo.setSelectedIndex(0);
        typeCombo.setSelectedIndex(0);
        descriptionField.setText("");
        dateField.setText(LocalDate.now().plusDays(3).toString());
        statusCombo.setSelectedIndex(0);
        reminderTable.clearSelection();
    }

    private void addReminder() {
        if (loggedInUser == null) return;
        Object pObj = patientCombo.getSelectedItem();
        String patient = pObj != null ? pObj.toString().trim() : "";
        String desc = descriptionField.getText().trim();
        String rDate = dateField.getText().trim();

        if (patient.isEmpty() || desc.isEmpty() || rDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate.parse(rDate);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Reminder reminder = new Reminder(
                0,
                loggedInUser.getId(),
                null,
                patient,
                (String) typeCombo.getSelectedItem(),
                desc,
                rDate,
                (String) statusCombo.getSelectedItem()
        );

        try {
            if (reminderDAO.addReminder(reminder)) {
                JOptionPane.showMessageDialog(this, "Healthcare reminder added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadReminders();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding reminder: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateReminder() {
        if (loggedInUser == null || selectedReminderId == null) {
            JOptionPane.showMessageDialog(this, "Select a reminder from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object pObj = patientCombo.getSelectedItem();
        String patient = pObj != null ? pObj.toString().trim() : "";
        String desc = descriptionField.getText().trim();
        String rDate = dateField.getText().trim();

        if (patient.isEmpty() || desc.isEmpty() || rDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate.parse(rDate);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format (use YYYY-MM-DD).", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Reminder reminder = new Reminder(
                selectedReminderId,
                loggedInUser.getId(),
                null,
                patient,
                (String) typeCombo.getSelectedItem(),
                desc,
                rDate,
                (String) statusCombo.getSelectedItem()
        );

        try {
            if (reminderDAO.updateReminder(reminder)) {
                JOptionPane.showMessageDialog(this, "Reminder updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadReminders();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating reminder: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markComplete() {
        if (loggedInUser == null || selectedReminderId == null) {
            JOptionPane.showMessageDialog(this, "Select a reminder from the table to mark as completed.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (reminderDAO.markCompleted(selectedReminderId, loggedInUser.getId())) {
                JOptionPane.showMessageDialog(this, "Reminder marked as Completed!", "Completed", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadReminders();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error marking completed: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteReminder() {
        if (loggedInUser == null || selectedReminderId == null) {
            JOptionPane.showMessageDialog(this, "Select a reminder from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this reminder?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (reminderDAO.deleteReminder(selectedReminderId, loggedInUser.getId())) {
                    JOptionPane.showMessageDialog(this, "Reminder deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadReminders();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting reminder: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new ReminderFrame().setVisible(true);
        });
    }
}