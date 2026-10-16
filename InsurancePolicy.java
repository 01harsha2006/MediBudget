package MediBudget;

/**
 * Model representing a health insurance policy.
 */
public class InsurancePolicy {

    private int id;
    private int userId;
    private String provider;
    private String policyNumber;
    private String policyType = "Health Floater";
    private double coverageAmount;
    private double premiumAmount = 0.0;
    private String expiryDate = "";
    private String notes = "";

    // Existing constructor preserved for backwards compatibility
    public InsurancePolicy(String provider, String policyNumber,
                           double coverageAmount) {
        this.provider = provider;
        this.policyNumber = policyNumber;
        this.coverageAmount = coverageAmount;
    }

    // Full constructor for persistence
    public InsurancePolicy(int id, int userId, String provider, String policyNumber,
                           String policyType, double coverageAmount, double premiumAmount,
                           String expiryDate, String notes) {
        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.policyNumber = policyNumber;
        this.policyType = policyType;
        this.coverageAmount = coverageAmount;
        this.premiumAmount = premiumAmount;
        this.expiryDate = expiryDate;
        this.notes = notes;
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    public double getCoverageAmount() {
        return coverageAmount;
    }

    public void setCoverageAmount(double coverageAmount) {
        this.coverageAmount = coverageAmount;
    }

    public double getPremiumAmount() {
        return premiumAmount;
    }

    public void setPremiumAmount(double premiumAmount) {
        this.premiumAmount = premiumAmount;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void displayDetails() {
        System.out.println("----------------------------------------");
        System.out.println("Provider        : " + provider);
        System.out.println("Policy Number   : " + policyNumber);
        System.out.println("Policy Type     : " + policyType);
        System.out.println("Coverage Amount : ₹" + coverageAmount);
        System.out.println("Premium Amount  : ₹" + premiumAmount);
        System.out.println("Expiry Date     : " + expiryDate);
    }

    @Override
    public String toString() {
        return provider + " - " + policyNumber + " (₹" + coverageAmount + ")";
    }
}