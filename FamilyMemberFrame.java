package MediBudget;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Family Member Management Frame with full CRUD, MySQL persistence,
 * styled JTable, and user isolation.
 */
public class FamilyMemberFrame extends JFrame {

    private DashboardFrame dashboard;
    private User loggedInUser;
    private FamilyMemberDAO familyDAO = new FamilyMemberDAO();

    private JTextField nameField;
    private JTextField ageField;
    private JComboBox<String> relationshipCombo;
    private JComboBox<String> genderCombo;
    private JComboBox<String> bloodGroupCombo;
    private JTextField notesField;
    private JTextField searchField;

    private JTable memberTable;
    private DefaultTableModel tableModel;
    private Integer selectedMemberId = null;

    private static final String[] RELATIONSHIPS = {
            "Self", "Spouse", "Father", "Mother", "Son", "Daughter", "Brother", "Sister", "Grandparent", "Other"
    };

    private static final String[] GENDERS = {"Male", "Female", "Other", "Not Specified"};
    private static final String[] BLOOD_GROUPS = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"};

    public FamilyMemberFrame() {
        this(null);
    }

    public FamilyMemberFrame(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        this.loggedInUser = dashboard != null ? dashboard.getLoggedInUser() : null;

        setTitle("MediBudget - Family Member Management");
        setSize(920, 680);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadFamilyMembers();
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBackground(UITheme.BG_PAGE);

        JPanel header = UITheme.createHeaderBanner(
                "Family Members",
                "Manage household dependents and their health profiles",
                loggedInUser != null ? "User: " + loggedInUser.getName() : null
        );
        contentPane.add(header, BorderLayout.NORTH);

        // Center Split: Left Form (Add/Update) / Right Table
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(340);
        splitPane.setDividerSize(6);
        splitPane.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        splitPane.setBackground(UITheme.BG_PAGE);

        // LEFT: Form Card
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);

        JLabel lblFormTitle = new JLabel("Member Details", SwingConstants.LEFT);
        lblFormTitle.setFont(UITheme.FONT_SECTION);
        lblFormTitle.setForeground(UITheme.TEXT_DARK);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formCard.add(lblFormTitle, gbc);

        gbc.gridwidth = 1;

        nameField = UITheme.createTextField();
        ageField = UITheme.createTextField();
        relationshipCombo = UITheme.createComboBox(RELATIONSHIPS);
        genderCombo = UITheme.createComboBox(GENDERS);
        bloodGroupCombo = UITheme.createComboBox(BLOOD_GROUPS);
        notesField = UITheme.createTextField();

        addFormField(formCard, gbc, 1, "Full Name *", nameField);
        addFormField(formCard, gbc, 2, "Age *", ageField);
        addFormField(formCard, gbc, 3, "Relationship", relationshipCombo);
        addFormField(formCard, gbc, 4, "Gender", genderCombo);
        addFormField(formCard, gbc, 5, "Blood Group", bloodGroupCombo);
        addFormField(formCard, gbc, 6, "Medical Notes", notesField);

        // Buttons: Add, Update, Clear
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 8, 6, 8);

        JPanel btnFormPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnFormPanel.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("Add Member");
        JButton btnUpdate = UITheme.createSecondaryButton("Update Selected");
        JButton btnDelete = UITheme.createDangerButton("Delete Selected");
        JButton btnClear = UITheme.createNeutralButton("Clear Form");

        btnAdd.addActionListener(e -> addMember());
        btnUpdate.addActionListener(e -> updateMember());
        btnDelete.addActionListener(e -> deleteMember());
        btnClear.addActionListener(e -> clearForm());

        btnFormPanel.add(btnAdd);
        btnFormPanel.add(btnUpdate);
        btnFormPanel.add(btnDelete);
        btnFormPanel.add(btnClear);

        formCard.add(btnFormPanel, gbc);
        splitPane.setLeftComponent(formCard);

        // RIGHT: Table Panel with Search
        JPanel rightPanel = UITheme.createCardPanel();
        rightPanel.setLayout(new BorderLayout(8, 8));

        // Search Bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UITheme.FONT_BODY_BOLD);
        searchField = UITheme.createTextField();
        searchField.setPreferredSize(new Dimension(180, 28));
        JButton btnSearch = UITheme.createSecondaryButton("Search");
        JButton btnReset = UITheme.createNeutralButton("Reset");

        btnSearch.addActionListener(e -> filterTable(searchField.getText().trim()));
        btnReset.addActionListener(e -> {
            searchField.setText("");
            loadFamilyMembers();
        });

        searchBar.add(lblSearch);
        searchBar.add(searchField);
        searchBar.add(btnSearch);
        searchBar.add(btnReset);
        rightPanel.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Name", "Age", "Relationship", "Gender", "Blood Group", "Notes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        memberTable = new JTable(tableModel);
        UITheme.styleTable(memberTable);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Row selection listener to populate edit form
        memberTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = memberTable.getSelectedRow();
                if (selectedRow != -1) {
                    populateFormFromRow(selectedRow);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(memberTable);
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

    private void loadFamilyMembers() {
        if (loggedInUser == null) return;
        tableModel.setRowCount(0);
        try {
            List<FamilyMember> members = familyDAO.getFamilyMembers(loggedInUser.getId());
            for (FamilyMember m : members) {
                tableModel.addRow(new Object[]{
                        m.getId(),
                        m.getName(),
                        m.getAge(),
                        m.getRelationship(),
                        m.getGender(),
                        m.getBloodGroup(),
                        m.getNotes()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error while loading family members:\n" + ex.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable(String keyword) {
        if (keyword.isEmpty()) {
            loadFamilyMembers();
            return;
        }
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            String name = tableModel.getValueAt(i, 1).toString().toLowerCase();
            String rel = tableModel.getValueAt(i, 3).toString().toLowerCase();
            if (!name.contains(keyword.toLowerCase()) && !rel.contains(keyword.toLowerCase())) {
                tableModel.removeRow(i);
            }
        }
    }

    private void populateFormFromRow(int row) {
        selectedMemberId = (Integer) memberTable.getValueAt(row, 0);
        nameField.setText(memberTable.getValueAt(row, 1).toString());
        ageField.setText(memberTable.getValueAt(row, 2).toString());
        relationshipCombo.setSelectedItem(memberTable.getValueAt(row, 3).toString());
        genderCombo.setSelectedItem(memberTable.getValueAt(row, 4).toString());
        bloodGroupCombo.setSelectedItem(memberTable.getValueAt(row, 5).toString());
        notesField.setText(memberTable.getValueAt(row, 6).toString());
    }

    private void clearForm() {
        selectedMemberId = null;
        nameField.setText("");
        ageField.setText("");
        relationshipCombo.setSelectedIndex(0);
        genderCombo.setSelectedIndex(0);
        bloodGroupCombo.setSelectedIndex(0);
        notesField.setText("");
        memberTable.clearSelection();
    }

    private void addMember() {
        if (loggedInUser == null) return;
        String name = nameField.getText().trim();
        String ageText = ageField.getText().trim();

        if (name.isEmpty() || ageText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both name and age.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age <= 0 || age > 125) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid age between 1 and 125.", "Invalid Age", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FamilyMember member = new FamilyMember(
                0,
                loggedInUser.getId(),
                name,
                age,
                (String) relationshipCombo.getSelectedItem(),
                (String) genderCombo.getSelectedItem(),
                (String) bloodGroupCombo.getSelectedItem(),
                notesField.getText().trim()
        );

        try {
            if (familyDAO.addFamilyMember(member)) {
                JOptionPane.showMessageDialog(this, "Family member added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadFamilyMembers();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding member: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMember() {
        if (loggedInUser == null || selectedMemberId == null) {
            JOptionPane.showMessageDialog(this, "Please select a member from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = nameField.getText().trim();
        String ageText = ageField.getText().trim();

        if (name.isEmpty() || ageText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in required fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age <= 0 || age > 125) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid age.", "Invalid Age", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FamilyMember member = new FamilyMember(
                selectedMemberId,
                loggedInUser.getId(),
                name,
                age,
                (String) relationshipCombo.getSelectedItem(),
                (String) genderCombo.getSelectedItem(),
                (String) bloodGroupCombo.getSelectedItem(),
                notesField.getText().trim()
        );

        try {
            if (familyDAO.updateFamilyMember(member)) {
                JOptionPane.showMessageDialog(this, "Family member updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadFamilyMembers();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating member: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteMember() {
        if (loggedInUser == null || selectedMemberId == null) {
            JOptionPane.showMessageDialog(this, "Please select a member from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this family member?\nAssociated records will be detached.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (familyDAO.deleteFamilyMember(selectedMemberId, loggedInUser.getId())) {
                    JOptionPane.showMessageDialog(this, "Family member deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadFamilyMembers();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting member: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new FamilyMemberFrame().setVisible(true);
        });
    }
}