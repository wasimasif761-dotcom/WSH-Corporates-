package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Product
import com.example.data.model.SaleTransaction
import com.example.data.model.UserAccount
import com.example.ui.components.AddEditProductDialog
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.components.AuthDialog
import com.example.ui.components.ChatbotDialog
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.InvoiceDialog
import com.example.ui.components.NavigationDrawerContent
import com.example.ui.components.OffersDialog
import com.example.ui.components.ProductDetailDialog
import com.example.ui.components.RecordSaleDialog
import com.example.ui.components.RefundExchangeDialog
import com.example.ui.components.StoreSettingsDialog
import com.example.ui.components.CrmProfilesDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AppScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.POSScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.InventoryViewModelFactory
import com.example.ui.viewmodel.StockFilter
import com.example.ui.viewmodel.UiEvent
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: InventoryViewModel by viewModels {
        val app = application as InventraApplication
        InventoryViewModelFactory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

    // Dialog States
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    var showRecordSaleDialog by remember { mutableStateOf(false) }
    var preselectedSaleProduct by remember { mutableStateOf<Product?>(null) }

    var viewingProduct by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showStoreSettingsDialog by remember { mutableStateOf(false) }
    var showOffersDialog by remember { mutableStateOf(false) }
    var showCrmDialog by remember { mutableStateOf(false) }
    var showChatbotDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showRefundExchangeDialog by remember { mutableStateOf(false) }
    var refundPreselectedTx by remember { mutableStateOf<SaleTransaction?>(null) }

    // State Collection
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val dashboardStats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val recentTransactions by viewModel.recentTransactions.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val posCart by viewModel.posCart.collectAsStateWithLifecycle()
    val activeInvoicePreview by viewModel.activeInvoicePreview.collectAsStateWithLifecycle()
    val storeSettings by viewModel.storeSettings.collectAsStateWithLifecycle()
    val discountOffers by viewModel.discountOffers.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val stockFilter by viewModel.stockFilter.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()

    // Listen to ViewModel events (Snackbars)
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    // Hardware back press handling
    BackHandler(enabled = drawerState.isOpen || currentScreen != AppScreen.DASHBOARD) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (currentScreen != AppScreen.DASHBOARD) {
            currentScreen = AppScreen.DASHBOARD
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                currentScreen = currentScreen,
                lowStockCount = dashboardStats.lowStockCount,
                developerName = viewModel.developerName,
                storeSettings = storeSettings,
                onNavigate = { screen ->
                    currentScreen = screen
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenStoreSettings = {
                    coroutineScope.launch { drawerState.close() }
                    showStoreSettingsDialog = true
                },
                onOpenOffers = {
                    coroutineScope.launch { drawerState.close() }
                    showOffersDialog = true
                },
                onOpenCRM = {
                    coroutineScope.launch { drawerState.close() }
                    showCrmDialog = true
                },
                onResetDemoData = {
                    coroutineScope.launch { drawerState.close() }
                    viewModel.resetToDemoData()
                },
                onShowAbout = {
                    coroutineScope.launch { drawerState.close() }
                    showAboutDialog = true
                }
            )
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                AppTopBar(
                    currentScreen = currentScreen,
                    lowStockCount = dashboardStats.lowStockCount,
                    developerName = viewModel.developerName,
                    currentUserRole = currentUser.role,
                    currentUserFullName = currentUser.fullName,
                    onMenuClick = {
                        coroutineScope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    onLowStockClick = {
                        viewModel.setStockFilter(StockFilter.LOW_STOCK)
                        currentScreen = AppScreen.PRODUCTS
                    },
                    onChatbotClick = { showChatbotDialog = true },
                    onAuthClick = { showAuthDialog = true }
                )
            },
            bottomBar = {
                AppBottomBar(
                    currentScreen = currentScreen,
                    lowStockCount = dashboardStats.lowStockCount,
                    cartItemCount = posCart.sumOf { it.quantity },
                    onTabSelected = { tab ->
                        currentScreen = tab
                    }
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> {
                        DashboardScreen(
                            stats = dashboardStats,
                            recentTransactions = recentTransactions,
                            storeSettings = storeSettings,
                            onNavigateToPos = { currentScreen = AppScreen.POS_TERMINAL },
                            onNavigateToProducts = { currentScreen = AppScreen.PRODUCTS },
                            onNavigateToSales = { currentScreen = AppScreen.SALES },
                            onNavigateToLowStock = {
                                viewModel.setStockFilter(StockFilter.LOW_STOCK)
                                currentScreen = AppScreen.PRODUCTS
                            },
                            onOpenAddProduct = {
                                editingProduct = null
                                showAddEditDialog = true
                            },
                            onOpenRecordSale = {
                                preselectedSaleProduct = null
                                showRecordSaleDialog = true
                            },
                            onSelectTransaction = { tx ->
                                val invoiceTxList = allTransactions.filter { it.invoiceNumber == tx.invoiceNumber }
                                viewModel.showInvoicePreview(invoiceTxList.ifEmpty { listOf(tx) })
                            }
                        )
                    }

                    AppScreen.POS_TERMINAL -> {
                        POSScreen(
                            products = allProducts,
                            cartItems = posCart,
                            developerName = viewModel.developerName,
                            storeSettings = storeSettings,
                            discountOffers = discountOffers,
                            allCustomers = allCustomers,
                            onScanCode = { code -> viewModel.scanBarcodeOrSku(code) },
                            onAddToCart = { product -> viewModel.addToCart(product) },
                            onUpdateQuantity = { id, qty -> viewModel.updateCartItemQuantity(id, qty) },
                            onRemoveFromCart = { id -> viewModel.removeFromCart(id) },
                            onClearCart = { viewModel.clearCart() },
                            onOpenOffers = { showOffersDialog = true },
                            onCheckout = { method, tax, disc, cust, custId, ptsRedeemed ->
                                viewModel.completePosCheckout(method, tax, disc, cust, custId, ptsRedeemed) { _, _ -> }
                            }
                        )
                    }

                    AppScreen.PRODUCTS -> {
                        ProductsScreen(
                            products = filteredProducts,
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            stockFilter = stockFilter,
                            sortOption = sortOption,
                            storeSettings = storeSettings,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onCategoryChange = { viewModel.setSelectedCategory(it) },
                            onStockFilterChange = { viewModel.setStockFilter(it) },
                            onSortOptionChange = { viewModel.setSortOption(it) },
                            onProductClick = { product -> viewingProduct = product },
                            onEditProduct = { product ->
                                editingProduct = product
                                showAddEditDialog = true
                            },
                            onDeleteProduct = { product -> productToDelete = product },
                            onRecordSaleForProduct = { product ->
                                preselectedSaleProduct = product
                                showRecordSaleDialog = true
                            },
                            onAdjustStock = { id, delta -> viewModel.adjustStock(id, delta) },
                            onAddNewProduct = {
                                editingProduct = null
                                showAddEditDialog = true
                            }
                        )
                    }

                    AppScreen.SALES -> {
                        SalesScreen(
                            transactions = allTransactions,
                            storeSettings = storeSettings,
                            onRecordSale = {
                                preselectedSaleProduct = null
                                showRecordSaleDialog = true
                            },
                            onSelectTransaction = { tx ->
                                val invoiceTxList = allTransactions.filter { it.invoiceNumber == tx.invoiceNumber }
                                viewModel.showInvoicePreview(invoiceTxList.ifEmpty { listOf(tx) })
                            }
                        )
                    }

                    AppScreen.ANALYTICS -> {
                        AnalyticsScreen(
                            stats = dashboardStats,
                            allProducts = allProducts,
                            onGeneratePO = { supplier, poText ->
                                viewModel.generatePurchaseOrder(supplier, poText)
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog: Add / Edit Product
    if (showAddEditDialog) {
        AddEditProductDialog(
            initialProduct = editingProduct,
            onDismiss = {
                showAddEditDialog = false
                editingProduct = null
            },
            onSave = { product ->
                if (editingProduct != null) {
                    viewModel.updateProduct(product) { success, _ ->
                        if (success) {
                            showAddEditDialog = false
                            editingProduct = null
                        }
                    }
                } else {
                    viewModel.createProduct(
                        name = product.name,
                        sku = product.sku,
                        barcode = product.barcode,
                        category = product.category,
                        quantity = product.quantity,
                        price = product.price,
                        costPrice = product.costPrice,
                        minStockThreshold = product.minStockThreshold,
                        unit = product.unit,
                        description = product.description
                    ) { success, _ ->
                        if (success) {
                            showAddEditDialog = false
                        }
                    }
                }
            }
        )
    }

    // Dialog: Record Sale
    if (showRecordSaleDialog) {
        RecordSaleDialog(
            products = allProducts,
            initialProduct = preselectedSaleProduct,
            storeSettings = storeSettings,
            onDismiss = {
                showRecordSaleDialog = false
                preselectedSaleProduct = null
            },
            onConfirmSale = { cartItems, paymentMethod, customerName, discountPercent ->
                // Add items to POS cart with their batch numbers
                viewModel.clearCart()
                cartItems.forEach { item ->
                    viewModel.addToCart(item.product, item.quantity, item.batchNo)
                }
                
                val subtotal = cartItems.sumOf { it.subtotal }
                val discountAmount = subtotal * (discountPercent / 100.0)

                // Finalize sale using POS checkout logic (groups all items under one invoice!)
                viewModel.completePosCheckout(
                    paymentMethod = paymentMethod,
                    taxPercent = storeSettings.vatRatePercent,
                    discountAmount = discountAmount,
                    customerName = customerName.ifBlank { "Walk-in Retail Customer" }
                ) { success, _ ->
                    if (success) {
                        showRecordSaleDialog = false
                        preselectedSaleProduct = null
                    }
                }
            }
        )
    }

    // Dialog: Product Details
    viewingProduct?.let { product ->
        ProductDetailDialog(
            product = product,
            onDismiss = { viewingProduct = null },
            onEdit = {
                editingProduct = product
                showAddEditDialog = true
            },
            onDelete = {
                productToDelete = product
            },
            onRecordSale = {
                preselectedSaleProduct = product
                showRecordSaleDialog = true
            }
        )
    }

    // Dialog: Confirm Delete
    productToDelete?.let { product ->
        ConfirmDeleteDialog(
            product = product,
            onDismiss = { productToDelete = null },
            onConfirm = {
                viewModel.deleteProduct(product)
                productToDelete = null
            }
        )
    }

    // Dialog: Full Invoice Preview with Saudi Arabia Terms and Multi-Currency
    activeInvoicePreview?.let { transactions ->
        InvoiceDialog(
            transactions = transactions,
            storeSettings = storeSettings,
            onRefundClick = {
                refundPreselectedTx = transactions.firstOrNull()
                showRefundExchangeDialog = true
            },
            onDismiss = {
                viewModel.dismissInvoicePreview()
            }
        )
    }

    // Dialog: Store Settings & Currency Config (Exportable for every store)
    if (showStoreSettingsDialog) {
        StoreSettingsDialog(
            currentSettings = storeSettings,
            allProducts = allProducts,
            allTransactions = allTransactions,
            onDismiss = { showStoreSettingsDialog = false },
            onSaveSettings = { updated -> viewModel.updateStoreSettings(updated) }
        )
    }

    // Dialog: Offers & Discounts Management
    if (showOffersDialog) {
        OffersDialog(
            offers = discountOffers,
            storeSettings = storeSettings,
            onDismiss = { showOffersDialog = false },
            onAddOffer = { newOffer -> viewModel.addDiscountOffer(newOffer) },
            onRemoveOffer = { code -> viewModel.removeDiscountOffer(code) }
        )
    }

    // Dialog: Customer CRM profiles (Square & Shopify Loyalty Engine)
    if (showCrmDialog) {
        CrmProfilesDialog(
            customers = allCustomers,
            onDismiss = { showCrmDialog = false },
            onAddCustomer = { name, phone, email ->
                viewModel.createCustomer(name, phone, email)
            },
            onDeleteCustomer = { customer ->
                viewModel.deleteCustomer(customer)
            }
        )
    }

    // Dialog: About System & Developer Credits
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About WSH Corporates", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "WSH Corporates Retail POS & Inventory Engine",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Developed by Waseem",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• Specialized for Retail Shops, Supermarkets & FMCG Outlets\n" +
                            "• Saudi Arabia ZATCA E-Invoicing & Simplified Tax Invoices\n" +
                            "• Official bilingual KSA Terms & Conditions (7d return, 14d exchange, 15% VAT)\n" +
                            "• Multi-Currency (SAR, USD, EUR, GBP, AED, KWD, PKR, INR)\n" +
                            "• Exportable for every store (CSV Catalog & Invoices exporter)\n" +
                            "• POS Barcode Scanner (USB/Bluetooth/Manual)\n" +
                            "• Discount Management & Promotional Offer Codes",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showAboutDialog = false },
                    modifier = Modifier.testTag("close_about_dialog")
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Dialog: Chatbot robot assistant
    if (showChatbotDialog) {
        ChatbotDialog(
            products = allProducts,
            transactions = allTransactions,
            storeSettings = storeSettings,
            currentUserRole = currentUser.role,
            developerName = viewModel.developerName,
            onDismiss = { showChatbotDialog = false }
        )
    }

    // Dialog: Auth center (Login & register business)
    if (showAuthDialog) {
        AuthDialog(
            currentUser = currentUser,
            allUsers = allUsers,
            developerName = viewModel.developerName,
            onDismiss = { showAuthDialog = false },
            onLogin = { user: String, pass: String, role: String -> viewModel.login(user, pass, role) },
            onSignUp = { newUser: UserAccount -> viewModel.registerUser(newUser) }
        )
    }

    // Dialog: Refund & Exchange Processing center
    if (showRefundExchangeDialog) {
        RefundExchangeDialog(
            initialTransaction = refundPreselectedTx,
            allTransactions = allTransactions,
            allProducts = allProducts,
            storeSettings = storeSettings,
            cashierName = currentUser.fullName,
            onDismiss = {
                showRefundExchangeDialog = false
                refundPreselectedTx = null
            },
            onConfirmRefund = { txId: Long, qty: Int, reason: String ->
                viewModel.processRefund(txId, qty, reason)
            },
            onConfirmExchange = { txId: Long, retQty: Int, newId: Long, newQty: Int, reason: String ->
                viewModel.processExchange(txId, retQty, newId, newQty, reason)
            }
        )
    }
}
