package MediBudget;

/**
 * Model representing planned or ongoing medical treatments.
 */
public class Treatment {

    private int id;
    private int userId;
    private Integer familyMemberId;
    private String patientName;
    private String treatmentName;
    private String hospital;
    private String doctorName = "";
    private double estimatedCost;
    private String status = "Planned";
    private String startDate = "";
    private String notes = "";

    // Existing constructor preserved for backwards compatibility
    public Treatment(String patientName, String treatmentName,
                     String hospital, double estimatedCost, String status) {
        this.patientName = patientName;
        this.treatmentName = treatmentName;
        this.hospital = hospital;
        this.estimatedCost = estimatedCost;
        this.status = status;
    }

    // Full constructor for persistence
    public Treatment(int id, int userId, Integer familyMemberId, String patientName,
                     String treatmentName, String hospital, String doctorName,
                     double estimatedCost, String status, String startDate, String notes) {
        this.id = id;
        this.userId = userId;
        this.familyMemberId = familyMemberId;
        this.patientName = patientName;
        this.treatmentName = treatmentName;
        this.hospital = hospital;
        this.doctorName = doctorName;
        this.estimatedCost = estimatedCost;
        this.status = status;
        this.startDate = startDate;
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

    public String getTreatmentName() {
        return treatmentName;
    }

    public void setTreatmentName(String treatmentName) {
        this.treatmentName = treatmentName;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void displayDetails() {
        System.out.println("----------------------------------------");
        System.out.println("Patient        : " + patientName);
        System.out.println("Treatment      : " + treatmentName);
        System.out.println("Hospital       : " + hospital);
        System.out.println("Doctor         : " + doctorName);
        System.out.println("Estimated Cost : ₹" + estimatedCost);
        System.out.println("Status         : " + status);
        System.out.println("Start Date     : " + startDate);
    }
}