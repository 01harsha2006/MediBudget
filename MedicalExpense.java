package MediBudget;

/**
 * Model representing an individual medical expense record.
 */
public class MedicalExpense {

    private int id;
    private int userId;
    private Integer familyMemberId;
    private String patientName;
    private String category;
    private String description;
    private double amount;
    private String expenseDate = "";
    private String paymentMode = "Cash";

    // Existing constructor preserved for backwards compatibility
    public MedicalExpense(String patientName, String category,
                          String description, double amount) {
        this.patientName = patientName;
        this.category = category;
        this.description = description;
        this.amount = amount;
    }

    // Full constructor for persistence
    public MedicalExpense(int id, int userId, Integer familyMemberId, String patientName,
                          String category, String description, double amount,
                          String expenseDate, String paymentMode) {
        this.id = id;
        this.userId = userId;
        this.familyMemberId = familyMemberId;
        this.patientName = patientName;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.paymentMode = paymentMode;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(String expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public void displayDetails() {
        System.out.println("----------------------------------------");
        System.out.println("Patient      : " + patientName);
        System.out.println("Category     : " + category);
        System.out.println("Description  : " + description);
        System.out.println("Amount       : ₹" + amount);
        System.out.println("Date         : " + expenseDate);
        System.out.println("Payment Mode : " + paymentMode);
    }
}