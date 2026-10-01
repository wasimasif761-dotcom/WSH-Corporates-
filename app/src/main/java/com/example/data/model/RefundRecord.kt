package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "refund_records")
data class RefundRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalInvoiceNumber: String,
    val type: String = "REFUND", // "REFUND" or "EXCHANGE"
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val refundTotalAmount: Double,
    val reason: String = "Customer return within 7 days",
    val cashierName: String = "Waseem",
    val customerName: String = "Retail Customer",
    val exchangedWithProductId: Long = 0L,
    val exchangedWithProductName: String = "",
    val priceDifferencePaid: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
) {
    val displayRecordNumber: String
        get() = if (type == "REFUND") "WSH-RFD-${1000 + id}" else "WSH-EXC-${1000 + id}"

    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US).format(Date(timestamp))
}
