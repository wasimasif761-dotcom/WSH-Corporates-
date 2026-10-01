package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sale_transactions")
data class SaleTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String = "",
    val productId: Long,
    val productName: String,
    val productSku: String,
    val productBarcode: String = "",
    val quantitySold: Int,
    val unitPrice: Double,
    val subtotal: Double = unitPrice * quantitySold,
    val taxAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val paymentMethod: String = "Cash",
    val cashierName: String = "Waseem",
    val customerName: String = "Walk-in Retail Customer",
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isRefunded: Boolean = false,
    val refundReason: String = "",
    val refundTimestamp: Long = 0L,
    val isExchange: Boolean = false,
    val originalInvoiceNumber: String = "",
    val businessName: String = "WSH Corporates Retail Store"
) {
    val displayInvoiceNumber: String
        get() = if (invoiceNumber.isNotBlank()) invoiceNumber else "WSH-INV-${1000 + id}"
}
