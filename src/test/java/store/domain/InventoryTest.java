package store.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

public class InventoryTest {

    @Test
    public void testIsProductWithPromotion_유효한프로모션() {
        Product product = new Product("Apple", 1000, 10, "Promo1");
        Promotion promotion = new Promotion("Promo1", 10, "2023-01-01", "2025-01-01");
        Inventory inventory = new Inventory(Arrays.asList(product), Arrays.asList(promotion));

        boolean result = inventory.isProductWithPromotion("Apple");

        assertTrue(result);
    }

    @Test
    public void testIsProductWithPromotion_만료된프로모션() {
        Product product = new Product("Banana", 500, 20, "Promo2");
        Promotion promotion = new Promotion("Promo2", 15, "2020-01-01", "2021-01-01");
        Inventory inventory = new Inventory(Arrays.asList(product), Arrays.asList(promotion));

        boolean result = inventory.isProductWithPromotion("Banana");

        assertFalse(result);
    }

    @Test
    public void testIsProductWithPromotion_프로모션없음() {
        Product product = new Product("Orange", 700, 15, "null");
        Inventory inventory = new Inventory(Arrays.asList(product), Arrays.asList());

        boolean result = inventory.isProductWithPromotion("Orange");

        assertFalse(result);
    }
}