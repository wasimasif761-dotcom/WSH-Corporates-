package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.screens.AppScreen
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CorporateBlue

@Composable
fun AppBottomBar(
    currentScreen: AppScreen,
    lowStockCount: Int,
    cartItemCount: Int,
    onTabSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("app_bottom_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        // Tab 1: Dashboard
        NavigationBarItem(
            selected = currentScreen == AppScreen.DASHBOARD,
            onClick = { onTabSelected(AppScreen.DASHBOARD) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Dashboard,
                    contentDescription = "Dashboard"
                )
            },
            label = { Text("Dashboard", fontWeight = if (currentScreen == AppScreen.DASHBOARD) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("bottom_nav_dashboard")
        )

        // Tab 2: POS Scanner Terminal
        NavigationBarItem(
            selected = currentScreen == AppScreen.POS_TERMINAL,
            onClick = { onTabSelected(AppScreen.POS_TERMINAL) },
            icon = {
                if (cartItemCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = CorporateBlue,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text("$cartItemCount")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "POS Scanner Terminal"
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "POS Scanner Terminal"
                    )
                }
            },
            label = { Text("POS", fontWeight = if (currentScreen == AppScreen.POS_TERMINAL) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("bottom_nav_pos")
        )

        // Tab 3: Inventory / Products
        NavigationBarItem(
            selected = currentScreen == AppScreen.PRODUCTS,
            onClick = { onTabSelected(AppScreen.PRODUCTS) },
            icon = {
                if (lowStockCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = AmberWarning,
                                contentColor = MaterialTheme.colorScheme.surface
                            ) {
                                Text("$lowStockCount")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = "Inventory"
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = "Inventory"
                    )
                }
            },
            label = { Text("Inventory", fontWeight = if (currentScreen == AppScreen.PRODUCTS) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("bottom_nav_products")
        )

        // Tab 4: Invoices Logs
        NavigationBarItem(
            selected = currentScreen == AppScreen.SALES,
            onClick = { onTabSelected(AppScreen.SALES) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = "Tax Invoices"
                )
            },
            label = { Text("Invoices", fontWeight = if (currentScreen == AppScreen.SALES) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("bottom_nav_sales")
        )
    }
}
