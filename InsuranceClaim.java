package MediBudget;

/**
 * Model representing an insurance claim against a health policy.
 */
public class InsuranceClaim {

    private int id;
    private int userId;
    private Integer policyId;
    private Integer familyMemberId;
    private String patientName;
    private double claimAmount;
    private double settledAmount = 0.0;
    private String status = "Submitted";
    private String claimDate = "";
    private String remarks = "";

    // Existing constructor preserved for backwards compatibility
    public InsuranceClaim(String patientName,
                          double claimAmount,
                          String status) {
        this.patientName = patientName;
        this.claimAmount = claimAmount;
        this.status = status;
    }

    // Full constructor for persistence
    public InsuranceClaim(int id, int userId, Integer policyId, Integer familyMemberId,
                          String patientName, double claimAmount, double settledAmount,
                          String status, String claimDate, String remarks) {
        this.id = id;
        this.userId = userId;
        this.policyId = policyId;
        this.familyMemberId = familyMemberId;
        this.patientName = patientName;
        this.claimAmount = claimAmount;
        this.settledAmount = settledAmount;
        this.status = status;
        this.claimDate = claimDate;
        this.remarks = remarks;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Integer policyId) {
        this.policyId = policyId;
    }

    public Integer getFamilyMemberId() {
        return familyMemberId;
    }

    public void setFamilyMemberId(Integer familyMemberId) {
        this.familyMemberId = familyMemberId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public double getClaimAmount() {
        return claimAmount;
    }

    public void setClaimAmount(double claimAmount) {
        this.claimAmount = claimAmount;
    }

    public double getSettledAmount() {
        return settledAmount;
    }

    public void setSettledAmount(double settledAmount) {
        this.settledAmount = settledAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(String claimDate) {
        this.claimDate = claimDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void displayDetails() {
        System.out.println("----------------------------------------");
        System.out.println("Patient        : " + patientName);
        System.out.println("Claim Amount   : ₹" + claimAmount);
        System.out.println("Settled Amount : ₹" + settledAmount);
        System.out.println("Status         : " + status);
        System.out.println("Claim Date     : " + claimDate);
    }
}