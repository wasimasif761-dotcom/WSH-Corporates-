package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Product
import com.example.data.model.StoreSettings
import com.example.data.repository.CartItem
import com.example.ui.theme.CorporateBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessBg
import com.example.ui.theme.RoseDanger
import java.util.Locale

@Composable
fun RecordSaleDialog(
    products: List<Product>,
    initialProduct: Product? = null,
    storeSettings: StoreSettings = StoreSettings(),
    onDismiss: () -> Unit,
    onConfirmSale: (cartItems: List<CartItem>, paymentMethod: String, customerName: String, discountPercent: Double) -> Unit
) {
    val cartItems = remember { mutableStateListOf<CartItem>() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(initialProduct) }
    var quantityInput by remember { mutableStateOf("1") }
    var batchNoInput by remember { mutableStateOf(initialProduct?.batchNo ?: "") }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    var customerName by remember { mutableStateOf("Walk-in Retail Customer") }
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    var discountText by remember { mutableStateOf("0") }

    // If initialProduct was passed, populate it immediately into cart if cart is empty
    LaunchedEffect(initialProduct) {
        if (initialProduct != null && cartItems.isEmpty()) {
            cartItems.add(
                CartItem(
                    product = initialProduct,
                    quantity = 1,
                    batchNo = initialProduct.batchNo.ifBlank { "B-2026-01" }
                )
            )
        }
    }

    val filteredProducts = remember(searchQuery, products) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val q = searchQuery.trim().lowercase()
            products.filter {
                it.name.lowercase().contains(q) ||
                    it.sku.lowercase().contains(q) ||
                    it.barcode.lowercase().contains(q) ||
                    it.category.lowercase().contains(q)
            }.take(8)
        }
    }

    val subtotal = remember(cartItems.toList()) { cartItems.sumOf { it.subtotal } }
    val taxRate = storeSettings.vatRatePercent
    val discountPercent = discountText.toDoubleOrNull() ?: 0.0
    val discountAmount = subtotal * (discountPercent / 100.0)
    val taxAmount = (subtotal - discountAmount).coerceAtLeast(0.0) * (taxRate / 100.0)
    val grandTotal = (subtotal - discountAmount + taxAmount).coerceAtLeast(0.0)

    fun addItemToInvoice(prod: Product, qty: Int, batch: String) {
        if (qty <= 0) return
        val existingIndex = cartItems.indexOfFirst { it.product.id == prod.id }
        if (existingIndex >= 0) {
            val existing = cartItems[existingIndex]
            val newQty = (existing.quantity + qty).coerceAtMost(prod.quantity)
            cartItems[existingIndex] = existing.copy(
                quantity = newQty,
                batchNo = batch.ifBlank { existing.batchNo }
            )
        } else {
            val cappedQty = qty.coerceAtMost(prod.quantity)
            cartItems.add(
                CartItem(
                    product = prod,
                    quantity = cappedQty,
                    batchNo = batch.ifBlank { prod.batchNo.ifBlank { "B-2026-01" } }
                )
            )
        }
        // Reset item entry inputs
        searchQuery = ""
        selectedProduct = null
        quantityInput = "1"
        batchNoInput = ""
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .imePadding()
                .padding(vertical = 12.dp)
                .testTag("record_sale_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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
                                .size(38.dp)
                                .background(CorporateBlue, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PointOfSale,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Create Sale Invoice",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "فاتورة مبيعات جديدة • 15% VAT (SAR)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldSuccessBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${cartItems.sumOf { it.quantity }} items",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Item Search & Scan Input Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Search & Add Product (ابحث عن الصنف أو امسح الباركود):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = {
                                        searchQuery = it
                                        // Auto-match exact barcode if typed/scanned
                                        val exact = products.find { p ->
                                            p.barcode.equals(it.trim(), ignoreCase = true) ||
                                                p.sku.equals(it.trim(), ignoreCase = true)
                                        }
                                        if (exact != null) {
                                            selectedProduct = exact
                                            batchNoInput = exact.batchNo.ifBlank { "B-2026-01" }
                                        }
                                    },
                                    placeholder = { Text("Type name, SKU, or barcode (e.g. pan, milk)...") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = CorporateBlue)
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("record_sale_search_input")
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { showBarcodeScanner = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CorporateBlue),
                                    modifier = Modifier.testTag("record_sale_scan_btn")
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Scan")
                                }
                            }

                            // Matching product dropdown list
                            if (filteredProducts.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(10.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(6.dp)) {
                                        filteredProducts.forEach { prod ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        selectedProduct = prod
                                                        batchNoInput = prod.batchNo.ifBlank { "B-2026-01" }
                                                        searchQuery = prod.name
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = prod.name,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                                    )
                                                    Text(
                                                        text = "${prod.displayCode} • Stock: ${prod.quantity} ${prod.unit}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Text(
                                                    text = storeSettings.formatPrice(prod.price),
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = CorporateBlue
                                                    )
                                                )
                                            }
                                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                        }
                                    }
                                }
                            }

                            // Selected Product Qty, Batch No, and Add button
                            selectedProduct?.let { prod ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = EmeraldSuccessBg.copy(alpha = 0.35f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = prod.name,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Unit Price: ${storeSettings.formatPrice(prod.price)} • Available: ${prod.quantity}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = EmeraldSuccess
                                                )
                                            }
                                            IconButton(onClick = { selectedProduct = null }) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Quantity with minus / plus
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1.2f)
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        val curr = quantityInput.toIntOrNull() ?: 1
                                                        if (curr > 1) quantityInput = (curr - 1).toString()
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                                }

                                                OutlinedTextField(
                                                    value = quantityInput,
                                                    onValueChange = { quantityInput = it.filter { ch -> ch.isDigit() } },
                                                    label = { Text("Qty") },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.width(60.dp)
                                                )

                                                IconButton(
                                                    onClick = {
                                                        val curr = quantityInput.toIntOrNull() ?: 1
                                                        if (curr < prod.quantity) quantityInput = (curr + 1).toString()
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                                }
                                            }

                                            // Batch Number
                                            OutlinedTextField(
                                                value = batchNoInput,
                                                onValueChange = { batchNoInput = it },
                                                label = { Text("Batch #") },
                                                placeholder = { Text("e.g. B-2026") },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f)
                                            )

                                            // Add to Invoice Button
                                            Button(
                                                onClick = {
                                                    val qty = quantityInput.toIntOrNull() ?: 1
                                                    addItemToInvoice(prod, qty, batchNoInput)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.testTag("btn_add_product_to_invoice")
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("+ Add", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Current Invoice Items List (أصناف الفاتورة)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = CorporateBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Invoice Items (${cartItems.size} items added):",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (cartItems.isNotEmpty()) {
                            Text(
                                text = "Clear All",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RoseDanger),
                                modifier = Modifier
                                    .clickable { cartItems.clear() }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (cartItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Bill is empty. Add 1 to 30 items above.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "All items will be grouped on ONE single invoice!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        // Table of Added Items (Stacked clearly)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        ) {
                            cartItems.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.6f)) {
                                        Text(
                                            text = "${index + 1}. ${item.product.name}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "[${item.product.displayCode}] • Batch: ${item.batchNo.ifBlank { "N/A" }}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${item.quantity} x ${storeSettings.formatPrice(item.product.price)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (item.quantity > 1) {
                                                    cartItems[index] = item.copy(quantity = item.quantity - 1)
                                                } else {
                                                    cartItems.removeAt(index)
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                                        }

                                        Text(
                                            text = "${item.quantity}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                if (item.quantity < item.product.quantity) {
                                                    cartItems[index] = item.copy(quantity = item.quantity + 1)
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Text(
                                            text = storeSettings.formatPrice(item.subtotal),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            modifier = Modifier.width(70.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                                        )

                                        IconButton(
                                            onClick = { cartItems.removeAt(index) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = RoseDanger, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                if (index < cartItems.size - 1) {
                                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Calculation & Totals Box
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
                                Text("Subtotal (المجموع الخاضع للضريبة):", style = MaterialTheme.typography.bodySmall)
                                Text(storeSettings.formatPrice(subtotal), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("VAT / الضريبة (${taxRate.toInt()}%):", style = MaterialTheme.typography.bodySmall)
                                Text(storeSettings.formatPrice(taxAmount), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            }

                            if (discountAmount > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Discount (الخصم):", style = MaterialTheme.typography.bodySmall, color = EmeraldSuccess)
                                    Text("- " + storeSettings.formatPrice(discountAmount), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = EmeraldSuccess))
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "GRAND TOTAL:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "المبلغ الإجمالي شامل الضريبة",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = storeSettings.formatPrice(grandTotal),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = CorporateBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Customer Name & Payment Method
                    Text(
                        text = "Customer Reference & Payment (العميل وطريقة الدفع):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name / Phone / VAT #") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Cash", "POS Card Terminal", "Digital Mada / ApplePay").forEach { method ->
                            FilterChip(
                                selected = selectedPaymentMethod == method,
                                onClick = { selectedPaymentMethod = method },
                                label = { Text(if (method == "Cash") "Cash (نقداً)" else if (method.contains("Card")) "MADA Card (مدى)" else "Apple Pay") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (method) {
                                            "Cash" -> Icons.Default.Payments
                                            "POS Card Terminal" -> Icons.Default.CreditCard
                                            else -> Icons.Default.PointOfSale
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Finalize Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (cartItems.isNotEmpty()) {
                                onConfirmSale(cartItems.toList(), selectedPaymentMethod, customerName, discountPercent)
                            }
                        },
                        enabled = cartItems.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("btn_finalize_invoice")
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Print Bill (${cartItems.size} items • ${storeSettings.formatPrice(grandTotal)})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            onDismiss = { showBarcodeScanner = false },
            onBarcodeScanned = { scannedCode ->
                showBarcodeScanner = false
                val found = products.find {
                    it.barcode.equals(scannedCode.trim(), ignoreCase = true) ||
                        it.sku.equals(scannedCode.trim(), ignoreCase = true)
                }
                if (found != null) {
                    selectedProduct = found
                    batchNoInput = found.batchNo.ifBlank { "B-2026-01" }
                    searchQuery = found.name
                } else {
                    searchQuery = scannedCode
                }
            }
        )
    }
}
