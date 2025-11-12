package oop.licao.smartpos.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cart.java
 * Represents the customer's shopping cart, managing cart items and totals.
 */
public class Cart {
    private final List<CartItem> items = new ArrayList<>();
    private double total;

    // Adds or updates an item in the cart
    public void addItem(Product product, int quantity) {
        Optional<CartItem> existing = getItemByProductId(product.getProductID());
        if (existing.isPresent()) {
            int newQty = existing.get().getQuantity() + quantity;
            existing.get().setQuantity(Math.max(newQty, 0));
            if (existing.get().getQuantity() == 0) {
                items.remove(existing.get());
            }
        } else {
            if (quantity > 0) items.add(new CartItem(product, quantity));
        }
        updateTotal();
    }

    // Removes an item completely from the cart
    public void removeItem(Product product) {
        items.removeIf(item -> item.getProduct().getProductID().equals(product.getProductID()));
        updateTotal();
    }

    // Directly sets item quantity (0 removes it)
    public void updateItemQuantity(Product product, int newQuantity) {
        Optional<CartItem> existing = getItemByProductId(product.getProductID());
        if (existing.isEmpty()) return;

        if (newQuantity <= 0) {
            items.remove(existing.get());
        } else {
            existing.get().setQuantity(newQuantity);
        }
        updateTotal();
    }

    // Finds an item in the cart by its product ID
    public Optional<CartItem> getItemByProductId(String productId) {
        return items.stream().filter(i -> i.getProduct().getProductID().equals(productId)).findFirst();
    }

    // Recalculates the total cart value
    public void updateTotal() {
        total = items.stream().mapToDouble(CartItem::getSubtotal).sum();
    }

    // Getters
    public double getTotal() { return total; }
    public List<CartItem> getItems() { return items; }

    // Clears the cart and resets total
    public void clear() {
        items.clear();
        total = 0;
    }
}
