package MediBudget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Data Access Object for Household Budget management and threshold alerting.
 */
public class BudgetDAO {

    private MedicalExpenseDAO expenseDAO = new MedicalExpenseDAO();

    public Budget getBudgetForMonth(int userId, int month, int year) throws SQLException {
        double spent = expenseDAO.getMonthlyExpenses(userId, month, year);

        String sql = "SELECT id, user_id, budget_month, budget_year, monthly_budget, alert_threshold_percent "
                   + "FROM budgets WHERE user_id = ? AND budget_month = ? AND budget_year = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Budget(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("budget_month"),
                            rs.getInt("budget_year"),
                            rs.getDouble("monthly_budget"),
                            spent,
                            rs.getInt("alert_threshold_percent")
                    );
                }
            }
        }
        // If not explicitly created, return default zero-budget holder
        return new Budget(0, userId, month, year, 0.00, spent, 80);
    }

    public Budget getCurrentMonthBudget(int userId) throws SQLException {
        LocalDate now = LocalDate.now();
        return getBudgetForMonth(userId, now.getMonthValue(), now.getYear());
    }

    public boolean setBudget(int userId, int month, int year, double budgetAmount, int alertThreshold) throws SQLException {
        String sql = "INSERT INTO budgets (user_id, budget_month, budget_year, monthly_budget, alert_threshold_percent) "
                   + "VALUES (?, ?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE monthly_budget = VALUES(monthly_budget), "
                   + "alert_threshold_percent = VALUES(alert_threshold_percent)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ps.setDouble(4, budgetAmount);
            ps.setInt(5, alertThreshold);
            return ps.executeUpdate() > 0;
        }
    }
}
