package oop.licao.smartpos.model;

/**
 * Product.java
 * Represents a product entity in the SmartPOS system.
 * Includes basic details such as name, price, stock, and category.
 */
public class Product {
    private String productID;
    private String productName;
    private double price;
    private int stock;
    private String description;
    private String imagePath;
    private String categoryID;

    // Required empty constructor for MongoDB POJO mapping
    public Product() {}

    public Product(String productID, String productName, double price, int stock,
                   String description, String imagePath, String categoryID) {
        this.productID = productID;
        this.productName = productName;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.imagePath = imagePath;
        this.categoryID = categoryID;
    }

    // Getters
    public String getProductID() { return productID; }
    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
    public String getCategoryID() { return categoryID; }

    // Setters
    public void setProductID(String productID) { this.productID = productID; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    public void setDescription(String description) { this.description = description; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setCategoryID(String categoryID) { this.categoryID = categoryID; }
}
