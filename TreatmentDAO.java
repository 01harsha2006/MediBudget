package MediBudget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Treatment Plans management.
 */
public class TreatmentDAO {

    public List<Treatment> getTreatments(int userId) throws SQLException {
        List<Treatment> list = new ArrayList<>();
        String sql = "SELECT id, user_id, family_member_id, patient_name, treatment_name, hospital, doctor_name, "
                   + "estimated_cost, status, start_date, notes "
                   + "FROM treatments WHERE user_id = ? ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int famId = rs.getInt("family_member_id");
                    Integer familyMemberId = rs.wasNull() ? null : famId;
                    String sDate = rs.getDate("start_date") != null ? rs.getDate("start_date").toString() : "";
                    list.add(new Treatment(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            familyMemberId,
                            rs.getString("patient_name"),
                            rs.getString("treatment_name"),
                            rs.getString("hospital"),
                            rs.getString("doctor_name"),
                            rs.getDouble("estimated_cost"),
                            rs.getString("status"),
                            sDate,
                            rs.getString("notes")
                    ));
                }
            }
        }
        return list;
    }

    public boolean addTreatment(Treatment treatment) throws SQLException {
        String sql = "INSERT INTO treatments (user_id, family_member_id, patient_name, treatment_name, hospital, doctor_name, estimated_cost, status, start_date, notes) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, treatment.getUserId());
            if (treatment.getFamilyMemberId() != null) {
                ps.setInt(2, treatment.getFamilyMemberId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, treatment.getPatientName());
            ps.setString(4, treatment.getTreatmentName());
            ps.setString(5, treatment.getHospital());
            ps.setString(6, treatment.getDoctorName());
            ps.setDouble(7, treatment.getEstimatedCost());
            ps.setString(8, treatment.getStatus());
            if (treatment.getStartDate() != null && !treatment.getStartDate().trim().isEmpty()) {
                ps.setDate(9, java.sql.Date.valueOf(treatment.getStartDate().trim()));
            } else {
                ps.setNull(9, java.sql.Types.DATE);
            }
            ps.setString(10, treatment.getNotes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        treatment.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateTreatment(Treatment treatment) throws SQLException {
        String sql = "UPDATE treatments SET patient_name = ?, treatment_name = ?, hospital = ?, doctor_name = ?, "
                   + "estimated_cost = ?, status = ?, start_date = ?, notes = ? "
                   + "WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, treatment.getPatientName());
            ps.setString(2, treatment.getTreatmentName());
            ps.setString(3, treatment.getHospital());
            ps.setString(4, treatment.getDoctorName());
            ps.setDouble(5, treatment.getEstimatedCost());
            ps.setString(6, treatment.getStatus());
            if (treatment.getStartDate() != null && !treatment.getStartDate().trim().isEmpty()) {
                ps.setDate(7, java.sql.Date.valueOf(treatment.getStartDate().trim()));
            } else {
                ps.setNull(7, java.sql.Types.DATE);
            }
            ps.setString(8, treatment.getNotes());
            ps.setInt(9, treatment.getId());
            ps.setInt(10, treatment.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteTreatment(int id, int userId) throws SQLException {
        String sql = "DELETE FROM treatments WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public int getActiveTreatmentsCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM treatments WHERE user_id = ? AND status IN ('Planned', 'Ongoing')";
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
