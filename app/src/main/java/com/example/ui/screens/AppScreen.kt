package com.example.ui.screens

enum class AppScreen(
    val title: String,
    val isBottomNavTab: Boolean = true
) {
    DASHBOARD("Dashboard", true),
    POS_TERMINAL("POS Terminal", true),
    PRODUCTS("Inventory", true),
    SALES("Invoices", true),
    ANALYTICS("Analytics", false)
}
