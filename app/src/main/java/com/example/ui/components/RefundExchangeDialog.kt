package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Product
import com.example.data.model.SaleTransaction
import com.example.data.model.StoreSettings
import com.example.ui.theme.CorporateBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseDanger

@Composable
fun RefundExchangeDialog(
    initialTransaction: SaleTransaction? = null,
    allTransactions: List<SaleTransaction>,
    allProducts: List<Product>,
    storeSettings: StoreSettings,
    cashierName: String,
    onDismiss: () -> Unit,
    onConfirmRefund: (txId: Long, qty: Int, reason: String) -> Unit,
    onConfirmExchange: (txId: Long, returnQty: Int, newProductId: Long, newQty: Int, reason: String) -> Unit
) {
    val context = LocalContext.current
    var selectedTx by remember { mutableStateOf(initialTransaction ?: allTransactions.firstOrNull()) }
    var searchInvoiceQuery by remember { mutableStateOf("") }

    var actionType by remember { mutableStateOf("REFUND") } // REFUND or EXCHANGE
    var returnQuantityText by remember { mutableStateOf("1") }
    var selectedReason by remember { mutableStateOf("Customer return within 7 days") }

    // Exchange fields
    var exchangeProductId by remember { mutableStateOf<Long?>(allProducts.firstOrNull { it.id != selectedTx?.productId }?.id) }
    var exchangeQuantityText by remember { mutableStateOf("1") }

    val returnQty = returnQuantityText.toIntOrNull() ?: 1
    val exchangeQty = exchangeQuantityText.toIntOrNull() ?: 1

    val exchangeProduct = allProducts.find { it.id == exchangeProductId }

    val refundAmount = if (selectedTx != null && selectedTx!!.quantitySold > 0) {
        (selectedTx!!.totalAmount / selectedTx!!.quantitySold) * returnQty
    } else 0.0

    val exchangeReplacementAmount = if (exchangeProduct != null) {
        exchangeProduct.price * exchangeQty * (1 + storeSettings.vatRatePercent / 100.0)
    } else 0.0

    val priceDiff = exchangeReplacementAmount - refundAmount

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .testTag("refund_exchange_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CorporateBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PublishedWithChanges,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Refund & Exchange Center",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "مركز الاسترجاع والاستبدال (KSA 7d/14d Policy)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Toggle: Refund vs Exchange
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = actionType == "REFUND",
                        onClick = { actionType = "REFUND" },
                        leadingIcon = { Icon(Icons.Default.CurrencyExchange, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Refund Money (استرجاع)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = actionType == "EXCHANGE",
                        onClick = { actionType = "EXCHANGE" },
                        leadingIcon = { Icon(Icons.Default.PublishedWithChanges, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Exchange Item (استبدال)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Invoice Selector
                Text(
                    text = "SELECT ORIGINAL INVOICE (اختيار الفاتورة)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (selectedTx != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = selectedTx!!.displayInvoiceNumber,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = storeSettings.formatPrice(selectedTx!!.totalAmount),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Item: ${selectedTx!!.productName} (${selectedTx!!.quantitySold} purchased)",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Customer: ${selectedTx!!.customerName} • Cashier: ${selectedTx!!.cashierName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity to Return
                OutlinedTextField(
                    value = returnQuantityText,
                    onValueChange = { returnQuantityText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Quantity to Return (الكمية المسترجعة)") },
                    supportingText = { Text("Purchased: ${selectedTx?.quantitySold ?: 1} units") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Reason Selection
                Text(
                    text = "REASON (سبب الاسترجاع)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                val reasons = listOf(
                    "Customer return within 7 days",
                    "Defective / Damaged product",
                    "Wrong item purchased",
                    "Dissatisfied with quality"
                )

                reasons.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = r }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (selectedReason == r) CorporateBlue else Color.LightGray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = r, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Exchange Section if EXCHANGE is selected
                if (actionType == "EXCHANGE") {
                    Text(
                        text = "SELECT REPLACEMENT PRODUCT (الصنف البديل)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CorporateBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    allProducts.filter { it.id != selectedTx?.productId }.take(4).forEach { p ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (exchangeProductId == p.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { exchangeProductId = p.id }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.name,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (exchangeProductId == p.id) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${p.quantity} in stock",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (exchangeProductId == p.id) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = storeSettings.formatPrice(p.price),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (exchangeProductId == p.id) Color.White else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Price Difference Breakdown
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Return Credit:", style = MaterialTheme.typography.bodySmall)
                                Text(storeSettings.formatPrice(refundAmount), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("New Item Value:", style = MaterialTheme.typography.bodySmall)
                                Text(storeSettings.formatPrice(exchangeReplacementAmount), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = if (priceDiff >= 0) "Customer Pays Extra:" else "Refund to Customer:",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = storeSettings.formatPrice(kotlin.math.abs(priceDiff)),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (priceDiff >= 0) CorporateBlue else EmeraldSuccess
                                    )
                                )
                            }
                        }
                    }
                } else {
                    // Refund Amount Summary
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Refund to Customer:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = storeSettings.formatPrice(refundAmount),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RoseDanger,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (selectedTx == null) {
                            Toast.makeText(context, "Please select an invoice first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (actionType == "REFUND") {
                            onConfirmRefund(selectedTx!!.id, returnQty, selectedReason)
                            Toast.makeText(context, "Refund processed & stock restored to shelf!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            if (exchangeProductId == null) {
                                Toast.makeText(context, "Please select an exchange replacement item", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onConfirmExchange(selectedTx!!.id, returnQty, exchangeProductId!!, exchangeQty, selectedReason)
                            Toast.makeText(context, "Exchange processed successfully!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (actionType == "REFUND") RoseDanger else CorporateBlue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_confirm_refund_exchange")
                ) {
                    Text(
                        text = if (actionType == "REFUND") "Confirm Refund & Restock Items" else "Confirm Exchange & Print Slip",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
