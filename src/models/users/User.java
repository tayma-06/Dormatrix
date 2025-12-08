package models.users;

public abstract class User {
    protected String id;
    protected String name;
    protected String role;
    protected String passwordHash;
    protected String phoneNumber;


    public User(String id, String name, String role, String passwordHash, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
    }

    // --------- Getters & Setters ----------
    public void setRole(String role) {
        this.role = role;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getPasswordHash() { return passwordHash; }

    public void setName(String name) { this.name = name; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    // --------- File Storage Formatting ----------
    public abstract String toFileString(); // Each subclass defines its file structure

}
