package MediBudget;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main Central Dashboard for MediBudget.
 * Styled with Modern Medical-Fintech Theme (Deep Navy, Slate, Medical Teal, and Emerald).
 * Features live KPI metrics, custom budget gauge, upcoming reminders,
 * and quick navigation grid.
 */
public class DashboardFrame extends JFrame {

    private User loggedInUser;

    // DAOs
    private MedicalExpenseDAO expenseDAO = new MedicalExpenseDAO();
    private BudgetDAO budgetDAO = new BudgetDAO();
    private FamilyMemberDAO familyDAO = new FamilyMemberDAO();
    private TreatmentDAO treatmentDAO = new TreatmentDAO();
    private InsuranceDAO insuranceDAO = new InsuranceDAO();
    private ReminderDAO reminderDAO = new ReminderDAO();

    // UI Elements for live metrics
    private JLabel lblTotalExpenseVal;
    private JLabel lblMonthlyExpenseVal;
    private JLabel lblBudgetRemainingVal;
    private JLabel lblFamilyCountVal;
    private JLabel lblActiveTreatmentsVal;
    private JLabel lblPendingClaimsVal;

    private JProgressBar budgetProgress;
    private JLabel lblBudgetSubtitle;
    private JTable upcomingRemindersTable;
    private DefaultTableModel remindersModel;

    public DashboardFrame(User user) {
        this.loggedInUser = user;

        setTitle("MediBudget - Executive Healthcare & Expense Dashboard");
        setSize(1100, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        refreshDashboardMetrics();
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBackground(UITheme.BG_PAGE);

        // 1. PRIMARY & NAVIGATION HEADER (Deep Navy #0F172A)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.HEADER_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.HEADER_SURFACE),
                BorderFactory.createEmptyBorder(18, 26, 18, 26)
        ));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel lblAppTitle = new JLabel("MediBudget Financial Protection Dashboard");
        lblAppTitle.setFont(UITheme.FONT_TITLE);
        lblAppTitle.setForeground(Color.WHITE);

        String deptStr = (loggedInUser != null && loggedInUser.getDepartment() != null && !loggedInUser.getDepartment().isEmpty())
                ? " | Dept: " + loggedInUser.getDepartment() : "";
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));

        JLabel lblSubTitle = new JLabel("Welcome back, " + (loggedInUser != null ? loggedInUser.getName() : "Guest")
                + deptStr + "  •  " + dateStr);
        lblSubTitle.setFont(UITheme.FONT_SUBTITLE);
        lblSubTitle.setForeground(UITheme.HEADER_SUBTITLE); // #94A3B8 Slate 400

        titleBox.add(lblAppTitle);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblSubTitle);

        headerPanel.add(titleBox, BorderLayout.WEST);

        // Header Right Action Buttons (Semi-transparent white/slate border with high-contrast text)
        JPanel actionBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBox.setOpaque(false);

        JButton btnRefresh = UITheme.createHeaderButton("Refresh");
        btnRefresh.addActionListener(e -> refreshDashboardMetrics());

        JButton btnDbSetup = UITheme.createHeaderButton("DB Settings");
        btnDbSetup.addActionListener(e -> new DatabaseConfigDialog(this).setVisible(true));

        JButton btnLogout = UITheme.createDangerButton("Logout");
        btnLogout.addActionListener(e -> logout());

        actionBox.add(btnRefresh);
        actionBox.add(btnDbSetup);
        actionBox.add(btnLogout);
        headerPanel.add(actionBox, BorderLayout.EAST);

        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Scrollable Body (Soft light slate #F8FAFC)
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(UITheme.BG_PAGE);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(12, 22, 22, 22));

        // 2. METRIC CARDS ROW (Standardized left-border accent system)
        JPanel kpiRow = new JPanel(new GridLayout(2, 3, 16, 16));
        kpiRow.setOpaque(false);

        lblTotalExpenseVal = new JLabel("₹0.00");
        lblMonthlyExpenseVal = new JLabel("₹0.00");
        lblBudgetRemainingVal = new JLabel("₹0.00");
        lblFamilyCountVal = new JLabel("0");
        lblActiveTreatmentsVal = new JLabel("0");
        lblPendingClaimsVal = new JLabel("0");

        kpiRow.add(createKpiCard("TOTAL MEDICAL EXPENSES", lblTotalExpenseVal, "Cumulative household spending", UITheme.ACCENT_TOTAL_EXPENSES));
        kpiRow.add(createKpiCard("THIS MONTH'S SPEND", lblMonthlyExpenseVal, LocalDate.now().getMonth().toString(), UITheme.ACCENT_MONTHLY_SPEND));
        kpiRow.add(createKpiCard("REMAINING MONTHLY BUDGET", lblBudgetRemainingVal, "Available limit before cap", UITheme.ACCENT_REMAINING_BUDGET));
        kpiRow.add(createKpiCard("FAMILY MEMBERS REGISTERED", lblFamilyCountVal, "Covered household dependents", UITheme.ACCENT_FAMILY_MEMBERS));
        kpiRow.add(createKpiCard("ACTIVE TREATMENTS", lblActiveTreatmentsVal, "Planned or ongoing procedures", UITheme.ACCENT_ACTIVE_TREATMENTS));
        kpiRow.add(createKpiCard("PENDING INSURANCE CLAIMS", lblPendingClaimsVal, "Awaiting approval / settlement", UITheme.ACCENT_PENDING_CLAIMS));

        bodyPanel.add(kpiRow);
        bodyPanel.add(Box.createVerticalStrut(20));

        // 3. BUDGET UTILIZATION BAR & CARD
        JPanel budgetCard = UITheme.createCardPanel();
        budgetCard.setLayout(new BorderLayout(10, 10));

        JPanel budgetTop = new JPanel(new BorderLayout());
        budgetTop.setOpaque(false);
        JLabel lblBudgetHead = new JLabel("Monthly Healthcare Budget Utilization");
        lblBudgetHead.setFont(UITheme.FONT_SECTION);
        lblBudgetHead.setForeground(UITheme.TEXT_DARK); // Dark Slate #0F172A

        lblBudgetSubtitle = new JLabel("Calculating monthly budget utilization...");
        lblBudgetSubtitle.setFont(UITheme.FONT_BODY);
        lblBudgetSubtitle.setForeground(UITheme.TEXT_MUTED);

        budgetTop.add(lblBudgetHead, BorderLayout.WEST);
        budgetTop.add(lblBudgetSubtitle, BorderLayout.EAST);
        budgetCard.add(budgetTop, BorderLayout.NORTH);

        // Custom Gradient Progress Bar (#0D9488 Teal to #10B981 Emerald with #E2E8F0 Track)
        budgetProgress = UITheme.createBudgetProgressBar();
        budgetCard.add(budgetProgress, BorderLayout.CENTER);

        bodyPanel.add(budgetCard);
        bodyPanel.add(Box.createVerticalStrut(20));

        // 4. MIDDLE SPLIT: Upcoming Reminders & Healthcare Modules
        JPanel middleSplit = new JPanel(new GridLayout(1, 2, 18, 0));
        middleSplit.setOpaque(false);

        // LEFT: Upcoming Reminders Widget
        JPanel reminderWidget = UITheme.createCardPanel();
        reminderWidget.setLayout(new BorderLayout(8, 8));

        JPanel reminderHead = new JPanel(new BorderLayout());
        reminderHead.setOpaque(false);
        JLabel lblRemHead = new JLabel("Upcoming Reminders & Critical Alerts");
        lblRemHead.setFont(UITheme.FONT_SECTION);
        lblRemHead.setForeground(UITheme.TEXT_DARK); // Dark Slate #0F172A
        reminderHead.add(lblRemHead, BorderLayout.WEST);

        JButton btnQuickDone = UITheme.createSuccessButton("Mark Done");
        btnQuickDone.addActionListener(e -> markSelectedReminderDone());
        reminderHead.add(btnQuickDone, BorderLayout.EAST);
        reminderWidget.add(reminderHead, BorderLayout.NORTH);

        String[] remCols = {"ID", "Patient", "Reminder", "Due Date"};
        remindersModel = new DefaultTableModel(remCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        upcomingRemindersTable = new JTable(remindersModel);
        UITheme.styleTable(upcomingRemindersTable);
        upcomingRemindersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane remScroll = new JScrollPane(upcomingRemindersTable);
        remScroll.setPreferredSize(new Dimension(420, 160));
        reminderWidget.add(remScroll, BorderLayout.CENTER);

        middleSplit.add(reminderWidget);

        // RIGHT: Module Action Cards
        JPanel navWidget = UITheme.createCardPanel();
        navWidget.setLayout(new BorderLayout(8, 10));

        JLabel lblNavHead = new JLabel("Healthcare Financial Management Modules");
        lblNavHead.setFont(UITheme.FONT_SECTION);
        lblNavHead.setForeground(UITheme.TEXT_DARK); // Dark Slate #0F172A
        navWidget.add(lblNavHead, BorderLayout.NORTH);

        JPanel navGrid = new JPanel(new GridLayout(3, 2, 12, 12));
        navGrid.setOpaque(false);

        JButton btnFamily = createNavButton("Family Members", "Manage profiles & dependents");
        JButton btnExpenses = createNavButton("Medical Expenses", "Log and categorize spending");
        JButton btnTreatment = createNavButton("Treatment Plans", "Monitor hospital procedures");
        JButton btnInsurance = createNavButton("Insurance & Claims", "Policies & reimbursements");
        JButton btnBudget = createNavButton("Budget Management", "Caps & inflation protection");
        JButton btnReminders = createNavButton("Healthcare Reminders", "Appointments & medications");

        btnFamily.addActionListener(e -> openFrame(new FamilyMemberFrame(this)));
        btnExpenses.addActionListener(e -> openFrame(new MedicalExpenseFrame(this)));
        btnTreatment.addActionListener(e -> openFrame(new TreatmentFrame(this)));
        btnInsurance.addActionListener(e -> openFrame(new InsuranceFrame(this)));
        btnBudget.addActionListener(e -> openFrame(new BudgetFrame(this)));
        btnReminders.addActionListener(e -> openFrame(new ReminderFrame(this)));

        navGrid.add(btnFamily);
        navGrid.add(btnExpenses);
        navGrid.add(btnTreatment);
        navGrid.add(btnInsurance);
        navGrid.add(btnBudget);
        navGrid.add(btnReminders);

        navWidget.add(navGrid, BorderLayout.CENTER);
        middleSplit.add(navWidget);

        bodyPanel.add(middleSplit);

        JScrollPane mainScroll = new JScrollPane(bodyPanel);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        contentPane.add(mainScroll, BorderLayout.CENTER);

        setContentPane(contentPane);
    }

    /**
     * Metric Card:
     * Card background #FFFFFF, border #E2E8F0, left accent border,
     * label muted slate #64748B (uppercase), value primary dark #0F172A.
     */
    private JPanel createKpiCard(String title, JLabel valLabel, String sub, Color accent) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(12, 14, 12, 14)
                )
        ));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_MUTED); // Slate 500 #64748B

        valLabel.setFont(UITheme.FONT_KPI_VAL);
        valLabel.setForeground(UITheme.TEXT_DARK); // Primary Dark #0F172A

        JLabel lblSub = new JLabel(sub);
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(lblTitle);
        content.add(Box.createVerticalStrut(4));
        content.add(valLabel);
        content.add(Box.createVerticalStrut(2));
        content.add(lblSub);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    /**
     * Module Action Card:
     * White background, #E2E8F0 border, hover state: #F0FDF4 with #0D9488 border!
     */
    private JButton createNavButton(String title, String subtitle) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(new Color(204, 251, 241)); // Light teal tint
                } else if (getModel().isRollover()) {
                    g2.setColor(UITheme.SUCCESS_BG);      // #F0FDF4 Very Light Emerald
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Border: Medical Teal #0D9488 on hover, otherwise #E2E8F0
                if (getModel().isRollover() || getModel().isPressed()) {
                    g2.setColor(UITheme.PRIMARY);
                } else {
                    g2.setColor(UITheme.BORDER_COLOR);
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setLayout(new BorderLayout(4, 2));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblT = new JLabel(title);
        lblT.setFont(UITheme.FONT_BODY_BOLD);
        lblT.setForeground(UITheme.TEXT_DARK);

        JLabel lblS = new JLabel(subtitle);
        lblS.setFont(UITheme.FONT_BODY);
        lblS.setForeground(UITheme.TEXT_MUTED);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.add(lblT);
        p.add(Box.createVerticalStrut(2));
        p.add(lblS);

        btn.add(p, BorderLayout.CENTER);
        return btn;
    }

    private void openFrame(JFrame frame) {
        frame.setVisible(true);
        setVisible(false);
    }

    public void refreshDashboardMetrics() {
        if (loggedInUser == null) return;
        int uid = loggedInUser.getId();

        try {
            // 1. Total expenses
            double totalExp = expenseDAO.getTotalExpenses(uid);
            lblTotalExpenseVal.setText(UITheme.formatCurrency(totalExp));

            // 2. Monthly expense
            double monthExp = expenseDAO.getCurrentMonthExpenses(uid);
            lblMonthlyExpenseVal.setText(UITheme.formatCurrency(monthExp));

            // 3. Budget & Remaining
            Budget curBudget = budgetDAO.getCurrentMonthBudget(uid);
            double remaining = curBudget.getRemainingAmount();
            lblBudgetRemainingVal.setText(UITheme.formatCurrency(remaining));
            lblBudgetRemainingVal.setForeground(UITheme.EMERALD_REMAINING); // Deep Emerald #047857

            if (curBudget.getBudgetAmount() > 0) {
                int percent = (int) Math.min(100, Math.max(0, curBudget.getPercentageUsed()));
                budgetProgress.setValue(percent);
                budgetProgress.setString(String.format("₹%.2f of ₹%.2f (%.1f%% Used)",
                        curBudget.getSpentAmount(), curBudget.getBudgetAmount(), curBudget.getPercentageUsed()));

                if (curBudget.getSpentAmount() > curBudget.getBudgetAmount()) {
                    lblBudgetSubtitle.setText("Exceeded by " + UITheme.formatCurrency(curBudget.getSpentAmount() - curBudget.getBudgetAmount()));
                    lblBudgetSubtitle.setForeground(UITheme.DANGER);
                } else if (curBudget.isAlertTriggered()) {
                    lblBudgetSubtitle.setText("Near Threshold (" + curBudget.getAlertThresholdPercent() + "% limit)");
                    lblBudgetSubtitle.setForeground(UITheme.WARNING);
                } else {
                    lblBudgetSubtitle.setText(UITheme.formatCurrency(remaining) + " remaining this month");
                    lblBudgetSubtitle.setForeground(UITheme.EMERALD_REMAINING); // Deep Emerald #047857
                }
            } else {
                budgetProgress.setValue(0);
                budgetProgress.setString("No budget cap set for this month");
                lblBudgetSubtitle.setText("Set a monthly budget cap in Budget Management");
                lblBudgetSubtitle.setForeground(UITheme.TEXT_MUTED);
            }

            // 4. Family count
            int famCount = familyDAO.getFamilyCount(uid);
            lblFamilyCountVal.setText(String.valueOf(famCount));

            // 5. Active treatments count
            int treatCount = treatmentDAO.getActiveTreatmentsCount(uid);
            lblActiveTreatmentsVal.setText(String.valueOf(treatCount));

            // 6. Pending claims count
            int claimsCount = insuranceDAO.getPendingClaimsCount(uid);
            lblPendingClaimsVal.setText(String.valueOf(claimsCount));

            // 7. Upcoming reminders table
            remindersModel.setRowCount(0);
            List<Reminder> upcoming = reminderDAO.getUpcomingReminders(uid, 6);
            for (Reminder r : upcoming) {
                remindersModel.addRow(new Object[]{
                        r.getId(),
                        r.getPatientName(),
                        r.getReminderType() + ": " + r.getDescription(),
                        r.getDate()
                });
            }

        } catch (SQLException ex) {
            System.err.println("[MediBudget Dashboard] Refresh metrics error: " + ex.getMessage());
        }
    }

    private void markSelectedReminderDone() {
        int row = upcomingRemindersTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reminder from the table to mark as done.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reminderId = (Integer) upcomingRemindersTable.getValueAt(row, 0);
        try {
            if (reminderDAO.markCompleted(reminderId, loggedInUser.getId())) {
                JOptionPane.showMessageDialog(this, "Reminder marked as completed!", "Completed", JOptionPane.INFORMATION_MESSAGE);
                refreshDashboardMetrics();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error completing reminder: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out of MediBudget?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            User testUser = new User(1, "Demo User", "Healthcare", "demo", "", "");
            DashboardFrame dashboard = new DashboardFrame(testUser);
            dashboard.setVisible(true);
        });
    }
}