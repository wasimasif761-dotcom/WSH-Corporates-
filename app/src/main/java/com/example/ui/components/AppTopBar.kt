package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AppScreen
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CorporateBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentScreen: AppScreen,
    lowStockCount: Int,
    developerName: String,
    currentUserRole: String,
    currentUserFullName: String,
    onMenuClick: () -> Unit,
    onLowStockClick: () -> Unit,
    onChatbotClick: () -> Unit,
    onAuthClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = when (currentScreen) {
                        AppScreen.DASHBOARD -> "Retail Dashboard"
                        AppScreen.POS_TERMINAL -> "POS Terminal"
                        AppScreen.PRODUCTS -> "Inventory Catalog"
                        AppScreen.SALES -> "Invoices & Logs"
                        AppScreen.ANALYTICS -> "Analytics & Valuation"
                    },
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "WSH Corporates • Dev: $developerName",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CorporateBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = currentUserRole.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                color = CorporateBlue
                            )
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("app_bar_menu_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open navigation drawer",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            // Chatbot robot assistant icon
            IconButton(
                onClick = onChatbotClick,
                modifier = Modifier.testTag("app_bar_chatbot_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Open AI chatbot",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // User Profile / Lock sign in switcher icon
            IconButton(
                onClick = onAuthClick,
                modifier = Modifier.testTag("app_bar_auth_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User profile login",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (lowStockCount > 0) {
                IconButton(
                    onClick = onLowStockClick,
                    modifier = Modifier.testTag("app_bar_low_stock_alert")
                ) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = AmberWarning,
                                contentColor = Color.Black
                            ) {
                                Text("$lowStockCount")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Low stock alert",
                            tint = AmberWarning,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.testTag("app_top_bar")
    )
}
