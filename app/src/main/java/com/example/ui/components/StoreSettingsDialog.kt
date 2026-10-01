package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.export.StoreExporter
import com.example.data.model.Product
import com.example.data.model.SaleTransaction
import com.example.data.model.StoreSettings
import com.example.data.model.SupportedCurrencies
import com.example.ui.theme.CorporateBlue

@Composable
fun StoreSettingsDialog(
    currentSettings: StoreSettings,
    allProducts: List<Product>,
    allTransactions: List<SaleTransaction>,
    onDismiss: () -> Unit,
    onSaveSettings: (StoreSettings) -> Unit
) {
    val context = LocalContext.current

    var storeName by remember { mutableStateOf(currentSettings.storeName) }
    var branchName by remember { mutableStateOf(currentSettings.branchName) }
    var vatNumber by remember { mutableStateOf(currentSettings.taxRegistrationNumber) }
    var crNumber by remember { mutableStateOf(currentSettings.commercialRegistration) }
    var phone by remember { mutableStateOf(currentSettings.phone) }
    var selectedCurrencyCode by remember { mutableStateOf(currentSettings.currencyCode) }
    var vatRateText by remember { mutableStateOf(currentSettings.vatRatePercent.toString()) }
    var termsText by remember { mutableStateOf(currentSettings.termsAndConditions) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .testTag("store_settings_dialog")
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
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = CorporateBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Store & Currency Settings",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Exportable for every retail outlet",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: Currency Selector (Multi-Currency)
                Text(
                    text = "SELECT STORE CURRENCY (اختيار العملة)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = CorporateBlue
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SupportedCurrencies.ALL.forEach { curr ->
                        FilterChip(
                            selected = selectedCurrencyCode.equals(curr.code, ignoreCase = true),
                            onClick = { selectedCurrencyCode = curr.code },
                            label = { Text("${curr.code} (${curr.symbol})") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Store Profile & Identification
                Text(
                    text = "STORE PROFILE & TAX REGISTRATION",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("Store Name (اسم المتجر)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_store_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = branchName,
                    onValueChange = { branchName = it },
                    label = { Text("Branch & City (الفرع والمدينة)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = vatNumber,
                        onValueChange = { vatNumber = it },
                        label = { Text("VAT / الرقم الضريبي") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )

                    OutlinedTextField(
                        value = vatRateText,
                        onValueChange = { vatRateText = it },
                        label = { Text("VAT %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = crNumber,
                        onValueChange = { crNumber = it },
                        label = { Text("CR # (السجل التجاري)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Store Phone") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Saudi Arabia Terms & Conditions Editor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TERMS & CONDITIONS (الشروط والأحكام)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Reset to KSA Standard",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CorporateBlue
                        ),
                        modifier = Modifier
                            .clickable {
                                termsText = StoreSettings.DEFAULT_KSA_TERMS
                                Toast.makeText(context, "Reset to Saudi Arabia ZATCA standard terms", Toast.LENGTH_SHORT).show()
                            }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = termsText,
                    onValueChange = { termsText = it },
                    label = { Text("Invoice Terms & Return Policy") },
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_terms_conditions")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Section 4: Exportable for Every Store (CSV Data Export)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "DATA EXPORTS (EVERY STORE COMPLIANT)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val csv = StoreExporter.generateProductsCsv(allProducts, currentSettings)
                                    StoreExporter.shareCsv(context, csv, "${storeName}_Inventory_Catalog.csv")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Inventory CSV", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val csv = StoreExporter.generateInvoicesCsv(allTransactions, currentSettings)
                                    StoreExporter.shareCsv(context, csv, "${storeName}_Tax_Invoices.csv")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Invoices CSV", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Action
                Button(
                    onClick = {
                        val parsedVat = vatRateText.toDoubleOrNull() ?: 15.0
                        val updated = currentSettings.copy(
                            storeName = storeName.trim().ifBlank { "WSH Corporates Retail Store" },
                            branchName = branchName.trim(),
                            taxRegistrationNumber = vatNumber.trim(),
                            commercialRegistration = crNumber.trim(),
                            phone = phone.trim(),
                            currencyCode = selectedCurrencyCode,
                            vatRatePercent = parsedVat,
                            termsAndConditions = termsText.trim()
                        )
                        onSaveSettings(updated)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_save_store_settings")
                ) {
                    Text("Save Store Settings & Currency", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
