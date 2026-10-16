package MediBudget;

/**
 * Model representing a household medical expense budget for a particular month/year.
 */
public class Budget {

    private int id;
    private int userId;
    private int month;
    private int year;
    private double budgetAmount;
    private double spentAmount;
    private int alertThresholdPercent = 80;

    // Existing constructor preserved
    public Budget(double budgetAmount) {
        this.budgetAmount = budgetAmount;
        this.spentAmount = 0;
    }

    // Full constructor for persistence
    public Budget(int id, int userId, int month, int year, double budgetAmount, double spentAmount, int alertThresholdPercent) {
        this.id = id;
        this.userId = userId;
        this.month = month;
        this.year = year;
        this.budgetAmount = budgetAmount;
        this.spentAmount = spentAmount;
        this.alertThresholdPercent = alertThresholdPercent;
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

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void addExpense(double amount) {
        spentAmount += amount;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public double getRemainingAmount() {
        return budgetAmount - spentAmount;
    }

    public int getAlertThresholdPercent() {
        return alertThresholdPercent;
    }

    public void setAlertThresholdPercent(int alertThresholdPercent) {
        this.alertThresholdPercent = alertThresholdPercent;
    }

    public double getPercentageUsed() {
        if (budgetAmount <= 0) return 0;
        return (spentAmount / budgetAmount) * 100.0;
    }

    public boolean isAlertTriggered() {
        return getPercentageUsed() >= alertThresholdPercent;
    }

    public void displayBudget() {
        System.out.println("----------------------------------------");
        System.out.println("Budget Amount     : ₹" + budgetAmount);
        System.out.println("Amount Spent      : ₹" + spentAmount);
        System.out.println("Remaining Amount  : ₹" + getRemainingAmount());
    }
}