package MediBudget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Family Members CRUD operations.
 */
public class FamilyMemberDAO {

    public List<FamilyMember> getFamilyMembers(int userId) throws SQLException {
        List<FamilyMember> list = new ArrayList<>();
        String sql = "SELECT id, user_id, name, age, relationship, gender, blood_group, notes "
                   + "FROM family_members WHERE user_id = ? ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new FamilyMember(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getInt("age"),
                            rs.getString("relationship"),
                            rs.getString("gender"),
                            rs.getString("blood_group"),
                            rs.getString("notes")
                    ));
                }
            }
        }
        return list;
    }

    public List<String> getFamilyMemberNames(int userId) throws SQLException {
        List<String> names = new ArrayList<>();
        String sql = "SELECT name FROM family_members WHERE user_id = ? ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("name"));
                }
            }
        }
        return names;
    }

    public boolean addFamilyMember(FamilyMember member) throws SQLException {
        String sql = "INSERT INTO family_members (user_id, name, age, relationship, gender, blood_group, notes) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, member.getUserId());
            ps.setString(2, member.getName());
            ps.setInt(3, member.getAge());
            ps.setString(4, member.getRelationship());
            ps.setString(5, member.getGender());
            ps.setString(6, member.getBloodGroup());
            ps.setString(7, member.getNotes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        member.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateFamilyMember(FamilyMember member) throws SQLException {
        String sql = "UPDATE family_members SET name = ?, age = ?, relationship = ?, gender = ?, blood_group = ?, notes = ? "
                   + "WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getName());
            ps.setInt(2, member.getAge());
            ps.setString(3, member.getRelationship());
            ps.setString(4, member.getGender());
            ps.setString(5, member.getBloodGroup());
            ps.setString(6, member.getNotes());
            ps.setInt(7, member.getId());
            ps.setInt(8, member.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteFamilyMember(int id, int userId) throws SQLException {
        String sql = "DELETE FROM family_members WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public int getFamilyCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM family_members WHERE user_id = ?";
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
