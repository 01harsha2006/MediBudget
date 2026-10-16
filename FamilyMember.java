package MediBudget;

/**
 * Model representing a family member in the household.
 */
public class FamilyMember {

    private int id;
    private int userId;
    private String name;
    private int age;
    private String relationship;
    private String gender = "Not Specified";
    private String bloodGroup = "Unknown";
    private String notes = "";

    // Existing constructor preserved for backwards compatibility
    public FamilyMember(String name, int age, String relationship) {
        this.name = name;
        this.age = age;
        this.relationship = relationship;
    }

    // Full constructor used by DAO and forms
    public FamilyMember(int id, int userId, String name, int age, String relationship,
                        String gender, String bloodGroup, String notes) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.relationship = relationship;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void displayDetails() {
        System.out.println("----------------------------------------");
        System.out.println("Name         : " + name);
        System.out.println("Age          : " + age);
        System.out.println("Relationship : " + relationship);
        System.out.println("Gender       : " + gender);
        System.out.println("Blood Group  : " + bloodGroup);
    }

    @Override
    public String toString() {
        return name + " (" + relationship + ")";
    }
}