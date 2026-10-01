package com.example.data.export

import android.content.Context
import android.content.Intent
import com.example.data.model.Product
import com.example.data.model.SaleTransaction
import com.example.data.model.StoreSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StoreExporter {

    fun generateProductsCsv(products: List<Product>, settings: StoreSettings): String {
        val sb = StringBuilder()
        sb.appendLine("sep=,")
        sb.appendLine("# STORE: ${settings.storeName}")
        sb.appendLine("# BRANCH: ${settings.branchName}")
        sb.appendLine("# VAT NUMBER: ${settings.taxRegistrationNumber}")
        sb.appendLine("# CURRENCY: ${settings.currencyCode}")
        sb.appendLine("# EXPORT DATE: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
        sb.appendLine("ID,Name,Barcode,SKU,Category,Quantity,Unit,RetailPrice,CostPrice,MinStockAlert,TotalValue")

        products.forEach { p ->
            val cleanName = escapeCsv(p.name)
            val cleanCat = escapeCsv(p.category)
            val cleanBarcode = escapeCsv(p.barcode)
            val cleanSku = escapeCsv(p.sku)
            sb.appendLine("${p.id},\"$cleanName\",\"$cleanBarcode\",\"$cleanSku\",\"$cleanCat\",${p.quantity},\"${p.unit}\",${p.price},${p.costPrice},${p.minStockThreshold},${p.totalValue}")
        }
        return sb.toString()
    }

    fun generateInvoicesCsv(transactions: List<SaleTransaction>, settings: StoreSettings): String {
        val sb = StringBuilder()
        sb.appendLine("sep=,")
        sb.appendLine("# STORE: ${settings.storeName}")
        sb.appendLine("# VAT NUMBER: ${settings.taxRegistrationNumber}")
        sb.appendLine("# CR NUMBER: ${settings.commercialRegistration}")
        sb.appendLine("# CURRENCY: ${settings.currencyCode}")
        sb.appendLine("# EXPORT DATE: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
        sb.appendLine("InvoiceNumber,Date,Customer,Cashier,Product,Barcode,Quantity,UnitPrice,Subtotal,Tax,Discount,GrandTotal,PaymentMethod,Notes")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        transactions.forEach { tx ->
            val dateStr = sdf.format(Date(tx.timestamp))
            sb.appendLine("\"${tx.displayInvoiceNumber}\",\"$dateStr\",\"${escapeCsv(tx.customerName)}\",\"${escapeCsv(tx.cashierName)}\",\"${escapeCsv(tx.productName)}\",\"${escapeCsv(tx.productBarcode)}\",${tx.quantitySold},${tx.unitPrice},${tx.subtotal},${tx.taxAmount},${tx.discountAmount},${tx.totalAmount},\"${escapeCsv(tx.paymentMethod)}\",\"${escapeCsv(tx.notes)}\"")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"").replace("\n", " ")
    }

    fun shareCsv(context: Context, csvContent: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, csvContent)
            type = "text/csv"
        }
        context.startActivity(Intent.createChooser(sendIntent, title))
    }
}
