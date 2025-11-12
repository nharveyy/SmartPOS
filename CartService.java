package oop.licao.smartpos.util;

import oop.licao.smartpos.model.Cart;
import oop.licao.smartpos.model.Product;

/**
 * CartService.java
 * Provides a globally accessible Cart instance and helper methods
 * for modifying items inside the cart.
 */
public class CartService {
    private static final Cart CART = new Cart(); // single shared cart instance

    // Returns the active cart
    public static Cart getCart() {
        return CART;
    }

    // Sets or updates product quantity in the cart
    public static void setQuantity(Product p, int qty) {
        CART.updateItemQuantity(p, qty);
    }

    // Removes a product from the cart
    public static void remove(Product p) {
        CART.removeItem(p);
    }
}
