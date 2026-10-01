package com.example

import com.example.data.model.Product
import com.example.data.model.StockStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryModelTest {

    @Test
    fun product_valuationCalculatesCorrectly() {
        val product = Product(
            id = 1,
            name = "Ergonomic Keyboard",
            sku = "KEY-ERG-01",
            category = "Electronics",
            quantity = 15,
            price = 80.0
        )

        assertEquals(1200.0, product.totalValue, 0.001)
    }

    @Test
    fun product_stockStatusClassification() {
        val normalProduct = Product(
            name = "USB Cable",
            sku = "USB-01",
            category = "Accessories",
            quantity = 20,
            price = 10.0,
            minStockThreshold = 5
        )
        assertEquals(StockStatus.IN_STOCK, normalProduct.stockStatus)
        assertFalse(normalProduct.isLowStock)
        assertFalse(normalProduct.isOutOfStock)

        val lowStockProduct = Product(
            name = "Thermal Paper",
            sku = "PPR-01",
            category = "Packaging Materials",
            quantity = 3,
            price = 5.0,
            minStockThreshold = 5
        )
        assertEquals(StockStatus.LOW_STOCK, lowStockProduct.stockStatus)
        assertTrue(lowStockProduct.isLowStock)
        assertFalse(lowStockProduct.isOutOfStock)

        val outOfStockProduct = Product(
            name = "Monitor Stand",
            sku = "STN-01",
            category = "Office Supplies",
            quantity = 0,
            price = 45.0,
            minStockThreshold = 5
        )
        assertEquals(StockStatus.OUT_OF_STOCK, outOfStockProduct.stockStatus)
        assertFalse(outOfStockProduct.isLowStock)
        assertTrue(outOfStockProduct.isOutOfStock)
    }
}
