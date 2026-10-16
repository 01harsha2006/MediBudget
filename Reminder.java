package MediBudget;

/**
 * Model representing medication, checkup, and renewal reminders.
 */
public class Reminder {

    private int id;
    private int userId;
    private Integer familyMemberId;
    private String patientName;
    private String reminderType;
    private String description;
    private String date;
    private String status = "Pending";

    // Existing constructor preserved for backwards compatibility
    public Reminder(String patientName, String reminderType,
                    String description, String date) {
        this.patientName = patientName;
        this.reminderType = reminderType;
        this.description = description;
        this.date = date;
    }

    // Full constructor for persistence
    public Reminder(int id, int userId, Integer familyMemberId, String patientName,
                    String reminderType, String description, String date, String status) {
        this.id = id;
        this.userId = userId;
        this.familyMemberId = familyMemberId;
        this.patientName = patientName;
        this.reminderType = reminderType;
        this.description = description;
        this.date = date;
        this.status = status;
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

    public String getReminderType() {
        return reminderType;
    }

    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void displayDetails() {
        System.out.println("----------------------------------------");
        System.out.println("Patient     : " + patientName);
        System.out.println("Type        : " + reminderType);
        System.out.println("Description : " + description);
        System.out.println("Date        : " + date);
        System.out.println("Status      : " + status);
    }
}