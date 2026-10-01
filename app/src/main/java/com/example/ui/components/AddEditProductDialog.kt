package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import com.example.data.model.CategoryConstants
import com.example.data.model.Product
import java.util.Locale
import kotlin.random.Random

@Composable
fun AddEditProductDialog(
    initialProduct: Product? = null,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    val isEditing = initialProduct != null

    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var sku by remember { mutableStateOf(initialProduct?.sku ?: "") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: CategoryConstants.DEFAULT_CATEGORIES.first()) }
    var quantityText by remember { mutableStateOf(initialProduct?.quantity?.toString() ?: "20") }
    var priceText by remember { mutableStateOf(initialProduct?.let { String.format(Locale.US, "%.2f", it.price) } ?: "4.99") }
    var costPriceText by remember { mutableStateOf(initialProduct?.let { String.format(Locale.US, "%.2f", it.costPrice) } ?: "3.20") }
    var minStockThresholdText by remember { mutableStateOf(initialProduct?.minStockThreshold?.toString() ?: "5") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "Pcs") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var expiryDateText by remember { mutableStateOf(initialProduct?.formattedExpiryDate?.let { if (it == "No Expiry") "" else it } ?: "") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var unitDropdownExpanded by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var skuError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }

    val retailUnits = listOf("Pcs", "Pack", "Bottle", "Box", "Kg", "Can", "Tin")

    fun generateRetailCodes() {
        val prefix = when {
            name.length >= 3 -> name.take(3).uppercase()
            else -> "RET"
        }
        val randomNum = Random.nextInt(1000, 9999)
        sku = "$prefix-$randomNum"
        barcode = "890" + Random.nextLong(1000000000L, 9999999999L).toString()
        skuError = null
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .testTag("add_edit_product_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEditing) "Edit Retail Item" else "New Retail Product",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "WSH Corporates Retail Inventory Catalog",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_dialog_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Product Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    label = { Text("Product / Item Name *") },
                    placeholder = { Text("e.g. Nestlé Nido Milk Powder 400g") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_name")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Barcode / Scanner field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("POS Barcode (EAN / UPC)") },
                        placeholder = { Text("e.g. 7613035391012") },
                        leadingIcon = {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_product_barcode")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { generateRetailCodes() },
                        modifier = Modifier.testTag("btn_generate_sku")
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Auto")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SKU Code
                OutlinedTextField(
                    value = sku,
                    onValueChange = {
                        sku = it.uppercase()
                        if (skuError != null) skuError = null
                    },
                    label = { Text("SKU Code *") },
                    placeholder = { Text("e.g. MLK-NID-400") },
                    isError = skuError != null,
                    supportingText = skuError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_sku")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category & Unit in 2 columns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1.5f)) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            readOnly = true,
                            label = { Text("Retail Category *") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Select category",
                                    modifier = Modifier.clickable { categoryDropdownExpanded = !categoryDropdownExpanded }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { categoryDropdownExpanded = true }
                                .testTag("input_product_category")
                        )

                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            CategoryConstants.DEFAULT_CATEGORIES.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Select unit",
                                    modifier = Modifier.clickable { unitDropdownExpanded = !unitDropdownExpanded }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { unitDropdownExpanded = true }
                        )

                        DropdownMenu(
                            expanded = unitDropdownExpanded,
                            onDismissRequest = { unitDropdownExpanded = false }
                        ) {
                            retailUnits.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unit = u
                                        unitDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity and Min Stock Alert
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = {
                            quantityText = it.filter { ch -> ch.isDigit() }
                            if (quantityError != null) quantityError = null
                        },
                        label = { Text("Shelf Stock *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = quantityError != null,
                        supportingText = quantityError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_product_quantity")
                    )

                    OutlinedTextField(
                        value = minStockThresholdText,
                        onValueChange = { minStockThresholdText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Min Stock Alert") },
                        supportingText = { Text("Restock threshold") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_min_stock")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price and Cost Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = {
                            priceText = it
                            if (priceError != null) priceError = null
                        },
                        label = { Text("Retail Price ($) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = priceError != null,
                        supportingText = priceError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_product_price")
                    )

                    OutlinedTextField(
                        value = costPriceText,
                        onValueChange = { costPriceText = it },
                        label = { Text("Cost Price ($)") },
                        supportingText = { Text("For margin analysis") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_product_cost")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Shelf / Aisle Location / Notes") },
                    placeholder = { Text("e.g. Aisle 3, Shelf B, Reorder from Distributor Alpha") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_description")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Expiry Date field
                OutlinedTextField(
                    value = expiryDateText,
                    onValueChange = { expiryDateText = it },
                    label = { Text("Expiry Date (تاريخ الانتهاء - YYYY-MM-DD)") },
                    placeholder = { Text("e.g. 2027-12-31 (or blank if none)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_expiry")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_cancel_product")
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            var hasError = false
                            if (name.isBlank()) {
                                nameError = "Name is required"
                                hasError = true
                            }
                            if (sku.isBlank()) {
                                skuError = "SKU is required"
                                hasError = true
                            }
                            val parsedPrice = priceText.toDoubleOrNull()
                            if (parsedPrice == null || parsedPrice < 0) {
                                priceError = "Enter valid price"
                                hasError = true
                            }
                            val parsedQty = quantityText.toIntOrNull()
                            if (parsedQty == null || parsedQty < 0) {
                                quantityError = "Enter valid quantity"
                                hasError = true
                            }

                            if (!hasError) {
                                val parsedExpiry = if (expiryDateText.isNotBlank()) {
                                    try {
                                        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(expiryDateText.trim())?.time ?: 0L
                                    } catch (e: Exception) {
                                        0L
                                    }
                                } else 0L

                                val productToSave = (initialProduct ?: Product(
                                    name = "",
                                    sku = "",
                                    category = "",
                                    quantity = 0,
                                    price = 0.0
                                )).copy(
                                    name = name.trim(),
                                    sku = sku.trim().uppercase(),
                                    barcode = barcode.trim(),
                                    category = category.trim(),
                                    quantity = parsedQty ?: 0,
                                    price = parsedPrice ?: 0.0,
                                    costPrice = costPriceText.toDoubleOrNull() ?: 0.0,
                                    minStockThreshold = minStockThresholdText.toIntOrNull() ?: 5,
                                    unit = unit.trim().ifBlank { "Pcs" },
                                    description = description.trim(),
                                    expiryDate = parsedExpiry,
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSave(productToSave)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("btn_save_product")
                    ) {
                        Text(if (isEditing) "Save Changes" else "Add to Catalog")
                    }
                }
            }
        }
    }
}
