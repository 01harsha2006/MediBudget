package MediBudget;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Budget Management Frame for planning monthly household medical expenditures,
 * tracking real-time spending against caps, and configuring alert thresholds.
 */
public class BudgetFrame extends JFrame {

    private DashboardFrame dashboard;
    private User loggedInUser;
    private BudgetDAO budgetDAO = new BudgetDAO();

    private JComboBox<String> monthCombo;
    private JComboBox<Integer> yearCombo;
    private JTextField budgetField;
    private JSlider thresholdSlider;
    private JLabel lblThresholdVal;

    private JLabel lblTotalSpent;
    private JLabel lblRemaining;
    private JLabel lblStatusBadge;
    private JProgressBar budgetProgressBar;

    private static final String[] MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    public BudgetFrame(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        this.loggedInUser = dashboard != null ? dashboard.getLoggedInUser() : null;

        setTitle("MediBudget - Household Budget Planner");
        setSize(780, 620);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
        loadCurrentBudgetData();
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBackground(UITheme.BG_PAGE);

        // Header
        JPanel header = UITheme.createHeaderBanner(
                "Household Medical Budget",
                "Set monthly expenditure caps and guard against healthcare inflation",
                loggedInUser != null ? "User: " + loggedInUser.getName() : null
        );
        contentPane.add(header, BorderLayout.NORTH);

        JPanel mainBody = new JPanel(new BorderLayout(15, 15));
        mainBody.setBackground(UITheme.BG_PAGE);
        mainBody.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Period Selection Bar
        JPanel periodPanel = UITheme.createCardPanel();
        periodPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 5));

        JLabel lblSelect = new JLabel("Budget Period:");
        lblSelect.setFont(UITheme.FONT_BODY_BOLD);
        lblSelect.setForeground(UITheme.TEXT_DARK);

        LocalDate now = LocalDate.now();
        monthCombo = UITheme.createComboBox(MONTH_NAMES);
        monthCombo.setSelectedIndex(now.getMonthValue() - 1);

        Integer[] years = new Integer[]{now.getYear() - 1, now.getYear(), now.getYear() + 1};
        yearCombo = UITheme.createComboBox(years);
        yearCombo.setSelectedItem(now.getYear());

        JButton btnLoad = UITheme.createSecondaryButton("Load Period");
        btnLoad.addActionListener(e -> loadCurrentBudgetData());

        monthCombo.addActionListener(e -> loadCurrentBudgetData());
        yearCombo.addActionListener(e -> loadCurrentBudgetData());

        periodPanel.add(lblSelect);
        periodPanel.add(new JLabel("Month:"));
        periodPanel.add(monthCombo);
        periodPanel.add(new JLabel("Year:"));
        periodPanel.add(yearCombo);
        periodPanel.add(btnLoad);

        mainBody.add(periodPanel, BorderLayout.NORTH);

        // Center Split: Left Form / Right Metrics
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        centerGrid.setOpaque(false);

        // Left Form: Set / Edit Budget
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);

        JLabel lblFormTitle = new JLabel("Configure Monthly Budget", SwingConstants.LEFT);
        lblFormTitle.setFont(UITheme.FONT_SECTION);
        lblFormTitle.setForeground(UITheme.TEXT_DARK);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formCard.add(lblFormTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        JLabel lblAmount = new JLabel("Budget Cap (₹):");
        lblAmount.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(lblAmount, gbc);

        gbc.gridx = 1;
        budgetField = UITheme.createTextField();
        formCard.add(budgetField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lblThreshold = new JLabel("Alert Warning (%):");
        lblThreshold.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(lblThreshold, gbc);

        gbc.gridx = 1;
        thresholdSlider = new JSlider(50, 100, 80);
        thresholdSlider.setBackground(Color.WHITE);
        thresholdSlider.setPaintTicks(true);
        thresholdSlider.setMajorTickSpacing(10);
        lblThresholdVal = new JLabel("80%", SwingConstants.RIGHT);
        lblThresholdVal.setFont(UITheme.FONT_BODY_BOLD);
        lblThresholdVal.setForeground(UITheme.PRIMARY);

        thresholdSlider.addChangeListener(e -> {
            lblThresholdVal.setText(thresholdSlider.getValue() + "%");
        });

        JPanel sliderBox = new JPanel(new BorderLayout(5, 0));
        sliderBox.setOpaque(false);
        sliderBox.add(thresholdSlider, BorderLayout.CENTER);
        sliderBox.add(lblThresholdVal, BorderLayout.EAST);
        formCard.add(sliderBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 8, 10);
        JButton btnSaveBudget = UITheme.createPrimaryButton("Save Budget Target");
        btnSaveBudget.addActionListener(e -> saveBudget());
        formCard.add(btnSaveBudget, gbc);

        centerGrid.add(formCard);

        // Right Card: Budget Status & Metrics
        JPanel metricsCard = UITheme.createCardPanel();
        metricsCard.setLayout(new BoxLayout(metricsCard, BoxLayout.Y_AXIS));

        JLabel lblMetricsTitle = new JLabel("Budget Utilization Status");
        lblMetricsTitle.setFont(UITheme.FONT_SECTION);
        lblMetricsTitle.setForeground(UITheme.TEXT_DARK);
        metricsCard.add(lblMetricsTitle);
        metricsCard.add(Box.createVerticalStrut(15));

        lblStatusBadge = new JLabel("Status: Unknown");
        lblStatusBadge.setFont(UITheme.FONT_BODY_BOLD);
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        metricsCard.add(lblStatusBadge);
        metricsCard.add(Box.createVerticalStrut(15));

        lblTotalSpent = new JLabel("Total Medical Spent: ₹0.00");
        lblTotalSpent.setFont(UITheme.FONT_BODY);
        metricsCard.add(lblTotalSpent);
        metricsCard.add(Box.createVerticalStrut(8));

        lblRemaining = new JLabel("Remaining Budget: ₹0.00");
        lblRemaining.setFont(UITheme.FONT_BODY_BOLD);
        metricsCard.add(lblRemaining);
        metricsCard.add(Box.createVerticalStrut(18));

        budgetProgressBar = UITheme.createBudgetProgressBar();
        metricsCard.add(budgetProgressBar);

        centerGrid.add(metricsCard);

        mainBody.add(centerGrid, BorderLayout.CENTER);

        // Bottom Navigation Bar
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

        contentPane.add(mainBody, BorderLayout.CENTER);
        contentPane.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private void loadCurrentBudgetData() {
        if (loggedInUser == null) return;

        int month = monthCombo.getSelectedIndex() + 1;
        int year = (Integer) yearCombo.getSelectedItem();

        try {
            Budget budget = budgetDAO.getBudgetForMonth(loggedInUser.getId(), month, year);
            budgetField.setText(String.format("%.2f", budget.getBudgetAmount()));
            thresholdSlider.setValue(budget.getAlertThresholdPercent());
            lblThresholdVal.setText(budget.getAlertThresholdPercent() + "%");

            lblTotalSpent.setText("Total Medical Spent: " + UITheme.formatCurrency(budget.getSpentAmount()));
            lblRemaining.setText("Remaining Budget: " + UITheme.formatCurrency(budget.getRemainingAmount()));
            lblRemaining.setForeground(budget.getRemainingAmount() >= 0 ? UITheme.EMERALD_REMAINING : UITheme.DANGER);

            int percent = (int) Math.min(100, Math.max(0, budget.getPercentageUsed()));
            budgetProgressBar.setValue(percent);
            budgetProgressBar.setString(String.format("%.1f%% Used", budget.getPercentageUsed()));

            if (budget.getBudgetAmount() <= 0) {
                lblStatusBadge.setText("No Budget Cap Configured");
                lblStatusBadge.setBackground(UITheme.BG_PAGE);
                lblStatusBadge.setForeground(UITheme.TEXT_MUTED);
                budgetProgressBar.setForeground(UITheme.TEXT_MUTED);
            } else if (budget.getSpentAmount() > budget.getBudgetAmount()) {
                lblStatusBadge.setText("BUDGET EXCEEDED by " + UITheme.formatCurrency(budget.getSpentAmount() - budget.getBudgetAmount()));
                lblStatusBadge.setBackground(UITheme.DANGER_BG);
                lblStatusBadge.setForeground(UITheme.DANGER);
                budgetProgressBar.setForeground(UITheme.DANGER);
            } else if (budget.isAlertTriggered()) {
                lblStatusBadge.setText("Warning: Spent " + String.format("%.1f%%", budget.getPercentageUsed()) + " (Threshold: " + budget.getAlertThresholdPercent() + "%)");
                lblStatusBadge.setBackground(UITheme.WARNING_BG);
                lblStatusBadge.setForeground(UITheme.WARNING);
                budgetProgressBar.setForeground(UITheme.WARNING);
            } else {
                lblStatusBadge.setText("Within Safe Spending Limits");
                lblStatusBadge.setBackground(UITheme.SUCCESS_BG);
                lblStatusBadge.setForeground(UITheme.SUCCESS);
                budgetProgressBar.setForeground(UITheme.SUCCESS);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load budget records: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveBudget() {
        if (loggedInUser == null) return;

        String amountText = budgetField.getText().trim();
        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid non-negative budget amount (e.g., 20000.00).",
                    "Invalid Budget",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int month = monthCombo.getSelectedIndex() + 1;
        int year = (Integer) yearCombo.getSelectedItem();
        int threshold = thresholdSlider.getValue();

        try {
            boolean ok = budgetDAO.setBudget(loggedInUser.getId(), month, year, amount, threshold);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Budget cap for " + MONTH_NAMES[month - 1] + " " + year + " saved successfully!",
                        "Budget Updated",
                        JOptionPane.INFORMATION_MESSAGE);
                loadCurrentBudgetData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error while saving budget: " + ex.getMessage(),
                    "Save Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
