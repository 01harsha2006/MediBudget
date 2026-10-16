package MediBudget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Reminders and alerts.
 */
public class ReminderDAO {

    public List<Reminder> getReminders(int userId) throws SQLException {
        List<Reminder> list = new ArrayList<>();
        String sql = "SELECT id, user_id, family_member_id, patient_name, reminder_type, description, reminder_date, status "
                   + "FROM reminders WHERE user_id = ? ORDER BY reminder_date ASC, id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int famId = rs.getInt("family_member_id");
                    Integer familyMemberId = rs.wasNull() ? null : famId;
                    String rDate = rs.getDate("reminder_date") != null ? rs.getDate("reminder_date").toString() : "";
                    list.add(new Reminder(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            familyMemberId,
                            rs.getString("patient_name"),
                            rs.getString("reminder_type"),
                            rs.getString("description"),
                            rDate,
                            rs.getString("status")
                    ));
                }
            }
        }
        return list;
    }

    public List<Reminder> getUpcomingReminders(int userId, int limit) throws SQLException {
        List<Reminder> list = new ArrayList<>();
        String sql = "SELECT id, user_id, family_member_id, patient_name, reminder_type, description, reminder_date, status "
                   + "FROM reminders WHERE user_id = ? AND status = 'Pending' "
                   + "ORDER BY reminder_date ASC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int famId = rs.getInt("family_member_id");
                    Integer familyMemberId = rs.wasNull() ? null : famId;
                    String rDate = rs.getDate("reminder_date") != null ? rs.getDate("reminder_date").toString() : "";
                    list.add(new Reminder(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            familyMemberId,
                            rs.getString("patient_name"),
                            rs.getString("reminder_type"),
                            rs.getString("description"),
                            rDate,
                            rs.getString("status")
                    ));
                }
            }
        }
        return list;
    }

    public boolean addReminder(Reminder reminder) throws SQLException {
        String sql = "INSERT INTO reminders (user_id, family_member_id, patient_name, reminder_type, description, reminder_date, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reminder.getUserId());
            if (reminder.getFamilyMemberId() != null) {
                ps.setInt(2, reminder.getFamilyMemberId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, reminder.getPatientName());
            ps.setString(4, reminder.getReminderType());
            ps.setString(5, reminder.getDescription());
            ps.setDate(6, java.sql.Date.valueOf(reminder.getDate()));
            ps.setString(7, reminder.getStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        reminder.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateReminder(Reminder reminder) throws SQLException {
        String sql = "UPDATE reminders SET patient_name = ?, reminder_type = ?, description = ?, "
                   + "reminder_date = ?, status = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reminder.getPatientName());
            ps.setString(2, reminder.getReminderType());
            ps.setString(3, reminder.getDescription());
            ps.setDate(4, java.sql.Date.valueOf(reminder.getDate()));
            ps.setString(5, reminder.getStatus());
            ps.setInt(6, reminder.getId());
            ps.setInt(7, reminder.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean markCompleted(int id, int userId) throws SQLException {
        String sql = "UPDATE reminders SET status = 'Completed' WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteReminder(int id, int userId) throws SQLException {
        String sql = "DELETE FROM reminders WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public int getPendingCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reminders WHERE user_id = ? AND status = 'Pending'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
