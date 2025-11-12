package oop.licao.smartpos.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO.java
 * Manages user authentication, registration, and admin account operations.
 */
public class UserDAO {

    private final MongoCollection<Document> userCollection;

    public UserDAO() {
        this.userCollection = MongoConnection.getDatabase().getCollection("User");
        ensureAdminExists(); // creates default admin if missing
    }

    // Ensures a default admin account exists
    private void ensureAdminExists() {
        Document admin = userCollection.find(Filters.eq("role", "admin")).first();
        if (admin == null) {
            Document newAdmin = new Document("firstName", "System")
                    .append("lastName", "Admin")
                    .append("email", "admin")
                    .append("password", "123")
                    .append("role", "admin");
            userCollection.insertOne(newAdmin);
            System.out.println("Default admin account created: admin / 123");
        }
    }

    // Registers a new user (customer)
    public boolean registerUser(String firstName, String lastName, String email, String password) {
        if (userCollection.find(Filters.eq("email", email)).first() != null) return false;

        Document doc = new Document("firstName", firstName)
                .append("lastName", lastName)
                .append("email", email)
                .append("password", password)
                .append("role", "customer");
        userCollection.insertOne(doc);
        return true;
    }

    // Validates login credentials and returns role (admin or customer)
    public String validateUserRole(String email, String password) {
        Document user = userCollection.find(Filters.eq("email", email)).first();
        if (user == null) return null;
        if (!password.equals(user.getString("password"))) return null;
        return user.getString("role");
    }

    // Retrieves all customer user records
    public List<Document> getAllUsers() {
        List<Document> users = new ArrayList<>();
        try (MongoCursor<Document> cursor = userCollection.find(Filters.eq("role", "customer")).iterator()) {
            while (cursor.hasNext()) users.add(cursor.next());
        }
        return users;
    }

    // Updates user profile information
    public void updateUser(String email, String newFirst, String newLast, String newPassword) {
        userCollection.updateOne(Filters.eq("email", email),
                Updates.combine(
                        Updates.set("firstName", newFirst),
                        Updates.set("lastName", newLast),
                        Updates.set("password", newPassword)
                ));
    }

    // Deletes a user by email
    public void deleteUser(String email) {
        userCollection.deleteOne(Filters.eq("email", email));
    }

    // Updates admin account password
    public void updateAdminPassword(String newPassword) {
        userCollection.updateOne(Filters.eq("role", "admin"),
                Updates.set("password", newPassword));
    }
}
