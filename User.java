package MediBudget;

/**
 * Represents an authenticated user in the MediBudget application.
 */
public class User {

    private int id;
    private String name;
    private String department;
    private String username;
    private String password;
    private String passwordHash;
    private String passwordSalt;

    // Existing constructor preserved for backwards compatibility
    public User(String name, String department, String username, String password) {
        this.name = name;
        this.department = department;
        this.username = username;
        this.password = password;
    }

    // Full constructor used by DAO
    public User(int id, String name, String department, String username, String passwordHash, String passwordSalt) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    @Override
    public String toString() {
        return name + " (" + username + ")";
    }
}