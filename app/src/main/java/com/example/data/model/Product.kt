package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sku: String,
    val barcode: String = "",
    val category: String,
    val quantity: Int,
    val price: Double,
    val costPrice: Double = 0.0,
    val minStockThreshold: Int = 5,
    val unit: String = "Pcs",
    val description: String = "",
    val expiryDate: Long = 0L, // Timestamp in milliseconds (0 = no expiry)
    val batchNo: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = quantity in 1..minStockThreshold

    val isOutOfStock: Boolean
        get() = quantity <= 0

    val totalValue: Double
        get() = quantity * price

    val stockStatus: StockStatus
        get() = when {
            quantity <= 0 -> StockStatus.OUT_OF_STOCK
            quantity <= minStockThreshold -> StockStatus.LOW_STOCK
            else -> StockStatus.IN_STOCK
        }

    val displayCode: String
        get() = barcode.ifBlank { sku }

    val hasExpiry: Boolean
        get() = expiryDate > 0L

    val isExpired: Boolean
        get() = hasExpiry && System.currentTimeMillis() > expiryDate

    val isExpiringSoon: Boolean
        get() = hasExpiry && !isExpired && (expiryDate - System.currentTimeMillis() <= 30L * 24 * 3600 * 1000)

    val formattedExpiryDate: String
        get() = if (hasExpiry) {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(expiryDate))
        } else {
            "No Expiry"
        }
}

enum class StockStatus {
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK
}
