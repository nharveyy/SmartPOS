package oop.licao.smartpos.model;

/**
 * CartItem.java
 * Represents a single item in the cart with its product and quantity.
 */
public class CartItem {
    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    // Calculates subtotal based on product price and quantity
    public double getSubtotal() {
        return product.getPrice() * quantity;
    }

    // Getters
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }

    // Sets quantity and ensures it is not negative
    public void setQuantity(int quantity) {
        this.quantity = Math.max(quantity, 0);
    }
}
