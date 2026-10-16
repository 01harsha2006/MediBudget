package MediBudget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Medical Expenses and budget spend aggregation.
 */
public class MedicalExpenseDAO {

    public List<MedicalExpense> getExpenses(int userId) throws SQLException {
        List<MedicalExpense> list = new ArrayList<>();
        String sql = "SELECT id, user_id, family_member_id, patient_name, category, description, amount, expense_date, payment_mode "
                   + "FROM medical_expenses WHERE user_id = ? ORDER BY expense_date DESC, id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int famId = rs.getInt("family_member_id");
                    Integer familyMemberId = rs.wasNull() ? null : famId;
                    list.add(new MedicalExpense(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            familyMemberId,
                            rs.getString("patient_name"),
                            rs.getString("category"),
                            rs.getString("description"),
                            rs.getDouble("amount"),
                            rs.getString("expense_date"),
                            rs.getString("payment_mode")
                    ));
                }
            }
        }
        return list;
    }

    public boolean addExpense(MedicalExpense expense) throws SQLException {
        String sql = "INSERT INTO medical_expenses (user_id, family_member_id, patient_name, category, description, amount, expense_date, payment_mode) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, expense.getUserId());
            if (expense.getFamilyMemberId() != null) {
                ps.setInt(2, expense.getFamilyMemberId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, expense.getPatientName());
            ps.setString(4, expense.getCategory());
            ps.setString(5, expense.getDescription());
            ps.setDouble(6, expense.getAmount());
            ps.setDate(7, java.sql.Date.valueOf(expense.getExpenseDate()));
            ps.setString(8, expense.getPaymentMode());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        expense.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateExpense(MedicalExpense expense) throws SQLException {
        String sql = "UPDATE medical_expenses SET patient_name = ?, category = ?, description = ?, amount = ?, expense_date = ?, payment_mode = ? "
                   + "WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, expense.getPatientName());
            ps.setString(2, expense.getCategory());
            ps.setString(3, expense.getDescription());
            ps.setDouble(4, expense.getAmount());
            ps.setDate(5, java.sql.Date.valueOf(expense.getExpenseDate()));
            ps.setString(6, expense.getPaymentMode());
            ps.setInt(7, expense.getId());
            ps.setInt(8, expense.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteExpense(int id, int userId) throws SQLException {
        String sql = "DELETE FROM medical_expenses WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public double getTotalExpenses(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM medical_expenses WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    public double getMonthlyExpenses(int userId, int month, int year) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM medical_expenses "
                   + "WHERE user_id = ? AND MONTH(expense_date) = ? AND YEAR(expense_date) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    public double getCurrentMonthExpenses(int userId) throws SQLException {
        LocalDate now = LocalDate.now();
        return getMonthlyExpenses(userId, now.getMonthValue(), now.getYear());
    }
}
