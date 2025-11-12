package oop.licao.smartpos.model;

/**
 * Category.java
 * Represents a product category for filtering and organization.
 */
public class Category {
    private String categoryID;
    private String categoryName;

    public Category(String categoryID, String categoryName) {
        this.categoryID = categoryID;
        this.categoryName = categoryName;
    }

    public String getCategoryID() { return categoryID; }
    public String getCategoryName() { return categoryName; }

    @Override
    public String toString() {
        return categoryName;
    }
}
