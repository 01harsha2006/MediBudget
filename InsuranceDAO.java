package MediBudget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Health Insurance Policies and Insurance Claims.
 */
public class InsuranceDAO {

    // ==========================================
    // POLICIES
    // ==========================================

    public List<InsurancePolicy> getPolicies(int userId) throws SQLException {
        List<InsurancePolicy> list = new ArrayList<>();
        String sql = "SELECT id, user_id, provider, policy_number, policy_type, coverage_amount, premium_amount, expiry_date, notes "
                   + "FROM insurance_policies WHERE user_id = ? ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String exp = rs.getDate("expiry_date") != null ? rs.getDate("expiry_date").toString() : "";
                    list.add(new InsurancePolicy(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("provider"),
                            rs.getString("policy_number"),
                            rs.getString("policy_type"),
                            rs.getDouble("coverage_amount"),
                            rs.getDouble("premium_amount"),
                            exp,
                            rs.getString("notes")
                    ));
                }
            }
        }
        return list;
    }

    public boolean addPolicy(InsurancePolicy policy) throws SQLException {
        String sql = "INSERT INTO insurance_policies (user_id, provider, policy_number, policy_type, coverage_amount, premium_amount, expiry_date, notes) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, policy.getUserId());
            ps.setString(2, policy.getProvider());
            ps.setString(3, policy.getPolicyNumber());
            ps.setString(4, policy.getPolicyType());
            ps.setDouble(5, policy.getCoverageAmount());
            ps.setDouble(6, policy.getPremiumAmount());
            if (policy.getExpiryDate() != null && !policy.getExpiryDate().trim().isEmpty()) {
                ps.setDate(7, java.sql.Date.valueOf(policy.getExpiryDate().trim()));
            } else {
                ps.setNull(7, java.sql.Types.DATE);
            }
            ps.setString(8, policy.getNotes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        policy.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updatePolicy(InsurancePolicy policy) throws SQLException {
        String sql = "UPDATE insurance_policies SET provider = ?, policy_number = ?, policy_type = ?, "
                   + "coverage_amount = ?, premium_amount = ?, expiry_date = ?, notes = ? "
                   + "WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, policy.getProvider());
            ps.setString(2, policy.getPolicyNumber());
            ps.setString(3, policy.getPolicyType());
            ps.setDouble(4, policy.getCoverageAmount());
            ps.setDouble(5, policy.getPremiumAmount());
            if (policy.getExpiryDate() != null && !policy.getExpiryDate().trim().isEmpty()) {
                ps.setDate(6, java.sql.Date.valueOf(policy.getExpiryDate().trim()));
            } else {
                ps.setNull(6, java.sql.Types.DATE);
            }
            ps.setString(7, policy.getNotes());
            ps.setInt(8, policy.getId());
            ps.setInt(9, policy.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletePolicy(int id, int userId) throws SQLException {
        String sql = "DELETE FROM insurance_policies WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    // ==========================================
    // CLAIMS
    // ==========================================

    public List<InsuranceClaim> getClaims(int userId) throws SQLException {
        List<InsuranceClaim> list = new ArrayList<>();
        String sql = "SELECT id, user_id, policy_id, family_member_id, patient_name, claim_amount, settled_amount, "
                   + "status, claim_date, remarks "
                   + "FROM insurance_claims WHERE user_id = ? ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int polId = rs.getInt("policy_id");
                    Integer policyId = rs.wasNull() ? null : polId;
                    int famId = rs.getInt("family_member_id");
                    Integer familyMemberId = rs.wasNull() ? null : famId;
                    String cDate = rs.getDate("claim_date") != null ? rs.getDate("claim_date").toString() : "";
                    list.add(new InsuranceClaim(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            policyId,
                            familyMemberId,
                            rs.getString("patient_name"),
                            rs.getDouble("claim_amount"),
                            rs.getDouble("settled_amount"),
                            rs.getString("status"),
                            cDate,
                            rs.getString("remarks")
                    ));
                }
            }
        }
        return list;
    }

    public boolean addClaim(InsuranceClaim claim) throws SQLException {
        String sql = "INSERT INTO insurance_claims (user_id, policy_id, family_member_id, patient_name, claim_amount, settled_amount, status, claim_date, remarks) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, claim.getUserId());
            if (claim.getPolicyId() != null) {
                ps.setInt(2, claim.getPolicyId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            if (claim.getFamilyMemberId() != null) {
                ps.setInt(3, claim.getFamilyMemberId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setString(4, claim.getPatientName());
            ps.setDouble(5, claim.getClaimAmount());
            ps.setDouble(6, claim.getSettledAmount());
            ps.setString(7, claim.getStatus());
            ps.setDate(8, java.sql.Date.valueOf(claim.getClaimDate()));
            ps.setString(9, claim.getRemarks());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        claim.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateClaim(InsuranceClaim claim) throws SQLException {
        String sql = "UPDATE insurance_claims SET patient_name = ?, claim_amount = ?, settled_amount = ?, "
                   + "status = ?, claim_date = ?, remarks = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, claim.getPatientName());
            ps.setDouble(2, claim.getClaimAmount());
            ps.setDouble(3, claim.getSettledAmount());
            ps.setString(4, claim.getStatus());
            ps.setDate(5, java.sql.Date.valueOf(claim.getClaimDate()));
            ps.setString(6, claim.getRemarks());
            ps.setInt(7, claim.getId());
            ps.setInt(8, claim.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteClaim(int id, int userId) throws SQLException {
        String sql = "DELETE FROM insurance_claims WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public int getPendingClaimsCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM insurance_claims WHERE user_id = ? AND status IN ('Submitted', 'Under Review')";
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
