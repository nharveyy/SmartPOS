package oop.licao.smartpos.util;

import oop.licao.smartpos.model.Category;

/**
 * CategoryUtil.java
 * Utility class for mapping category IDs to readable names.
 * Uses data from MockDataService for display purposes.
 */
public class CategoryUtil {

    // Returns category name based on category ID, or "Unknown" if not found
    public static String getCategoryNameById(String categoryId) {
        return MockDataService.getCategories().stream()
                .filter(cat -> cat.getCategoryID().equals(categoryId))
                .findFirst()
                .map(Category::getCategoryName)
                .orElse("Unknown");
    }
}
