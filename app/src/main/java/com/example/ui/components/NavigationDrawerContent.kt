package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoreSettings
import com.example.ui.screens.AppScreen
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CorporateBlue

@Composable
fun NavigationDrawerContent(
    currentScreen: AppScreen,
    lowStockCount: Int,
    developerName: String,
    storeSettings: StoreSettings,
    onNavigate: (AppScreen) -> Unit,
    onOpenStoreSettings: () -> Unit,
    onOpenOffers: () -> Unit,
    onOpenCRM: () -> Unit,
    onResetDemoData: () -> Unit,
    onShowAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .width(320.dp)
            .fillMaxHeight()
            .testTag("navigation_drawer_sheet"),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        // Corporate Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CorporateBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = storeSettings.storeName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = Color.White,
                            maxLines = 1
                        )
                        Text(
                            text = "Currency: ${storeSettings.currency.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lead Developer: $developerName",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Navigation Items
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            NavigationDrawerItem(
                label = { Text("Admin Dashboard") },
                icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                selected = currentScreen == AppScreen.DASHBOARD,
                onClick = { onNavigate(AppScreen.DASHBOARD) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .testTag("drawer_nav_dashboard")
            )

            NavigationDrawerItem(
                label = { Text("POS Barcode Terminal") },
                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                selected = currentScreen == AppScreen.POS_TERMINAL,
                onClick = { onNavigate(AppScreen.POS_TERMINAL) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .testTag("drawer_nav_pos")
            )

            NavigationDrawerItem(
                label = { Text("Retail Inventory Catalog") },
                icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                badge = {
                    if (lowStockCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(AmberWarning.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$lowStockCount Low",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AmberWarning
                            )
                        }
                    }
                },
                selected = currentScreen == AppScreen.PRODUCTS,
                onClick = { onNavigate(AppScreen.PRODUCTS) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .testTag("drawer_nav_products")
            )

            NavigationDrawerItem(
                label = { Text("Tax Invoices & Sales") },
                icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                selected = currentScreen == AppScreen.SALES,
                onClick = { onNavigate(AppScreen.SALES) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .testTag("drawer_nav_sales")
            )

            NavigationDrawerItem(
                label = { Text("Store Valuation & Analytics") },
                icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                selected = currentScreen == AppScreen.ANALYTICS,
                onClick = { onNavigate(AppScreen.ANALYTICS) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .testTag("drawer_nav_analytics")
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp))

            Text(
                text = "STORE & OFFERS CONFIG",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            NavigationDrawerItem(
                label = { Text("Store Profile & Currency") },
                icon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                selected = false,
                onClick = onOpenStoreSettings,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .testTag("drawer_store_settings")
            )

            NavigationDrawerItem(
                label = { Text("Promotions & Offers") },
                icon = { Icon(Icons.Default.LocalOffer, contentDescription = null) },
                selected = false,
                onClick = onOpenOffers,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .testTag("drawer_offers")
            )

            NavigationDrawerItem(
                label = { Text("CRM Customer Loyalty") },
                icon = { Icon(Icons.Default.Verified, contentDescription = null) },
                selected = false,
                onClick = onOpenCRM,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .testTag("drawer_crm")
            )

            NavigationDrawerItem(
                label = { Text("Load Retail Demo Data") },
                icon = { Icon(Icons.Default.RestartAlt, contentDescription = null) },
                selected = false,
                onClick = onResetDemoData,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .testTag("drawer_reset_data")
            )

            NavigationDrawerItem(
                label = { Text("About WSH Corporates") },
                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                selected = false,
                onClick = onShowAbout,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .testTag("drawer_about")
            )
        }
    }
}
