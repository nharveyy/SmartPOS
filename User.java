package oop.licao.smartpos.model;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

/**
 * User.java
 * Represents a user account in the SmartPOS system.
 * Used for authentication and role-based access (admin/customer).
 */
public class User {
    @BsonId
    private ObjectId id; // MongoDB internal ObjectId
    private String userID;
    private String fullName;
    private String username;
    private String password;
    private String role;

    public User(String userID, String fullName, String username, String password, String role) {
        this.userID = userID;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
}
