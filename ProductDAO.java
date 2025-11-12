package oop.licao.smartpos.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import oop.licao.smartpos.model.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * ProductDAO.java
 * Provides CRUD operations for products stored in MongoDB.
 */
public class ProductDAO {

    private final MongoCollection<Document> productCollection;

    public ProductDAO() {
        this.productCollection = MongoConnection.getDatabase().getCollection("products");
    }

    // Inserts a new product into MongoDB
    public void insert(Product product) {
        Document doc = new Document("productID", product.getProductID())
                .append("productName", product.getProductName())
                .append("description", product.getDescription())
                .append("price", product.getPrice())
                .append("categoryID", product.getCategoryID())
                .append("stock", product.getStock())
                .append("imagePath", product.getImagePath());
        productCollection.insertOne(doc);
    }

    // Updates existing product details
    public void updateProduct(Product product) {
        productCollection.updateOne(
                Filters.eq("productID", product.getProductID()),
                Updates.combine(
                        Updates.set("productName", product.getProductName()),
                        Updates.set("description", product.getDescription()),
                        Updates.set("price", product.getPrice()),
                        Updates.set("categoryID", product.getCategoryID()),
                        Updates.set("stock", product.getStock()),
                        Updates.set("imagePath", product.getImagePath())
                )
        );
    }

    // Deletes a product from MongoDB using its product ID
    public void deleteProduct(String productID) {
        productCollection.deleteOne(Filters.eq("productID", productID));
    }

    // Retrieves all products
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        try (MongoCursor<Document> cursor = productCollection.find().iterator()) {
            while (cursor.hasNext()) {
                products.add(fromDocument(cursor.next()));
            }
        }
        return products;
    }

    // Retrieves products filtered by category ID
    public List<Product> findByCategoryID(String categoryID) {
        List<Product> products = new ArrayList<>();
        try (MongoCursor<Document> cursor = productCollection.find(new Document("categoryID", categoryID)).iterator()) {
            while (cursor.hasNext()) {
                products.add(fromDocument(cursor.next()));
            }
        }
        return products;
    }

    // Finds a single product by its ID
    public Product findByID(String productID) {
        Document result = productCollection.find(Filters.eq("productID", productID)).first();
        if (result != null) return fromDocument(result);
        return null;
    }

    // Updates product stock quantity
    public void updateStock(String productID, int newStock) {
        productCollection.updateOne(
                Filters.eq("productID", productID),
                Updates.set("stock", newStock)
        );
    }

    // Converts a MongoDB document into a Product object
    private Product fromDocument(Document doc) {
        Product p = new Product();
        p.setProductID(doc.getString("productID"));
        p.setProductName(doc.getString("productName"));
        p.setDescription(doc.getString("description"));
        p.setPrice(doc.getDouble("price"));
        p.setCategoryID(doc.getString("categoryID"));
        p.setStock(doc.getInteger("stock", 0));
        p.setImagePath(doc.getString("imagePath"));
        return p;
    }
}
