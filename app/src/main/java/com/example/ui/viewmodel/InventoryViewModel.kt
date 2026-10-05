package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CustomerProfile
import com.example.data.model.DiscountOffer
import com.example.data.model.Product
import com.example.data.model.RefundRecord
import com.example.data.model.SaleTransaction
import com.example.data.model.StoreSettings
import com.example.data.model.SupportedCurrencies
import com.example.data.model.UserAccount
import com.example.data.repository.CartItem
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StockFilter {
    ALL,
    LOW_STOCK,
    OUT_OF_STOCK,
    IN_STOCK
}

enum class SortOption {
    NAME_ASC,
    NAME_DESC,
    STOCK_LOW_TO_HIGH,
    STOCK_HIGH_TO_LOW,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW
}

data class DashboardStats(
    val totalProducts: Int = 0,
    val totalUnits: Int = 0,
    val totalInventoryValue: Double = 0.0,
    val totalCostValue: Double = 0.0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalSalesTransactions: Int = 0,
    val categoryDistribution: Map<String, CategoryStat> = emptyMap()
)

data class CategoryStat(
    val category: String,
    val productCount: Int,
    val totalUnits: Int,
    val totalValue: Double
)

sealed interface UiEvent {
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : UiEvent
}

class InventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    val developerName = "Waseem"

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    // Store & Regional Settings (Default: Saudi Arabia ZATCA compliance, SAR currency)
    private val _storeSettings = MutableStateFlow(StoreSettings())
    val storeSettings: StateFlow<StoreSettings> = _storeSettings.asStateFlow()

    // Discount Offers & Promotions
    private val _discountOffers = MutableStateFlow(DiscountOffer.DEFAULT_OFFERS)
    val discountOffers: StateFlow<List<DiscountOffer>> = _discountOffers.asStateFlow()

    // Filters and Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _stockFilter = MutableStateFlow(StockFilter.ALL)
    val stockFilter: StateFlow<StockFilter> = _stockFilter.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.NAME_ASC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // POS Active Cart
    private val _posCart = MutableStateFlow<List<CartItem>>(emptyList())
    val posCart: StateFlow<List<CartItem>> = _posCart.asStateFlow()

    // Active Invoice Modal Preview
    private val _activeInvoicePreview = MutableStateFlow<List<SaleTransaction>?>(null)
    val activeInvoicePreview: StateFlow<List<SaleTransaction>?> = _activeInvoicePreview.asStateFlow()

    // Current Logged-in User (Admin or Cashier)
    private val _currentUser = MutableStateFlow(
        UserAccount(
            id = 1,
            username = "waseem",
            password = "123",
            fullName = "Waseem (Lead POS Developer & Admin)",
            role = "ADMIN",
            businessName = "WSH Corporates Retail Store"
        )
    )
    val currentUser: StateFlow<UserAccount> = _currentUser.asStateFlow()

    val allUsers: StateFlow<List<UserAccount>> = repository.allUsers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allRefunds: StateFlow<List<RefundRecord>> = repository.allRefunds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCustomers: StateFlow<List<CustomerProfile>> = repository.allCustomers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All raw products
    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All sales transactions
    val allTransactions: StateFlow<List<SaleTransaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Recent transactions (last 5)
    val recentTransactions: StateFlow<List<SaleTransaction>> = repository.recentTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered & Sorted Products for Products Screen
    val filteredProducts: StateFlow<List<Product>> = combine(
        allProducts,
        _searchQuery,
        _selectedCategory,
        _stockFilter,
        _sortOption
    ) { products, query, category, stock, sort ->
        products.filter { product ->
            val matchesQuery = query.isBlank() ||
                product.name.contains(query, ignoreCase = true) ||
                product.sku.contains(query, ignoreCase = true) ||
                product.barcode.contains(query, ignoreCase = true) ||
                product.description.contains(query, ignoreCase = true)

            val matchesCategory = category == "All" || product.category.equals(category, ignoreCase = true)

            val matchesStock = when (stock) {
                StockFilter.ALL -> true
                StockFilter.LOW_STOCK -> product.isLowStock
                StockFilter.OUT_OF_STOCK -> product.isOutOfStock
                StockFilter.IN_STOCK -> !product.isLowStock && !product.isOutOfStock
            }

            matchesQuery && matchesCategory && matchesStock
        }.let { list ->
            when (sort) {
                SortOption.NAME_ASC -> list.sortedBy { it.name.lowercase() }
                SortOption.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
                SortOption.STOCK_LOW_TO_HIGH -> list.sortedBy { it.quantity }
                SortOption.STOCK_HIGH_TO_LOW -> list.sortedByDescending { it.quantity }
                SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.price }
                SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.price }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Computed Dashboard Statistics
    val dashboardStats: StateFlow<DashboardStats> = combine(
        allProducts,
        allTransactions
    ) { products, transactions ->
        val totalProducts = products.size
        val totalUnits = products.sumOf { it.quantity }
        val totalInventoryValue = products.sumOf { it.quantity * it.price }
        val totalCostValue = products.sumOf { it.quantity * it.costPrice }
        val lowStockCount = products.count { it.isLowStock }
        val outOfStockCount = products.count { it.isOutOfStock }
        val totalRevenue = transactions.sumOf { it.totalAmount }
        val totalSalesTransactions = transactions.size

        val categoryMap = products.groupBy { it.category }.mapValues { (cat, items) ->
            CategoryStat(
                category = cat,
                productCount = items.size,
                totalUnits = items.sumOf { it.quantity },
                totalValue = items.sumOf { it.quantity * it.price }
            )
        }

        DashboardStats(
            totalProducts = totalProducts,
            totalUnits = totalUnits,
            totalInventoryValue = totalInventoryValue,
            totalCostValue = totalCostValue,
            lowStockCount = lowStockCount,
            outOfStockCount = outOfStockCount,
            totalRevenue = totalRevenue,
            totalSalesTransactions = totalSalesTransactions,
            categoryDistribution = categoryMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats()
    )

    // Store Settings Management
    fun updateStoreSettings(settings: StoreSettings) {
        _storeSettings.value = settings
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar("Store settings & currency updated"))
        }
    }

    fun setCurrency(currencyCode: String) {
        val curr = SupportedCurrencies.getByCode(currencyCode)
        _storeSettings.value = _storeSettings.value.copy(currencyCode = curr.code)
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar("Currency changed to ${curr.name} (${curr.symbol})"))
        }
    }

    // Offers & Discount Management
    fun addDiscountOffer(offer: DiscountOffer) {
        val current = _discountOffers.value.toMutableList()
        current.removeAll { it.code.equals(offer.code, ignoreCase = true) }
        current.add(0, offer)
        _discountOffers.value = current
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar("Offer '${offer.code}' created"))
        }
    }

    fun removeDiscountOffer(code: String) {
        _discountOffers.value = _discountOffers.value.filterNot { it.code.equals(code, ignoreCase = true) }
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar("Offer '$code' removed"))
        }
    }

    fun applyPromoOffer(code: String, subtotal: Double): Pair<DiscountOffer?, Double> {
        val match = _discountOffers.value.find { it.code.equals(code.trim(), ignoreCase = true) }
        return if (match != null) {
            val discountAmount = match.calculateDiscount(subtotal)
            Pair(match, discountAmount)
        } else {
            Pair(null, 0.0)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setStockFilter(filter: StockFilter) {
        _stockFilter.value = filter
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    // POS Cart Operations
    fun scanBarcodeOrSku(code: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val trimmed = code.trim()
            if (trimmed.isBlank()) {
                onResult(false, "Please enter or scan a barcode/SKU")
                return@launch
            }
            val product = repository.getProductByBarcodeOrSku(trimmed)
            if (product != null) {
                if (product.quantity <= 0) {
                    val msg = "Out of stock: ${product.name} (0 left)"
                    _eventFlow.emit(UiEvent.ShowSnackbar(msg, isError = true))
                    onResult(false, msg)
                } else {
                    addToCart(product)
                    val priceStr = _storeSettings.value.formatPrice(product.price)
                    val msg = "Scanned: ${product.name} • $priceStr"
                    _eventFlow.emit(UiEvent.ShowSnackbar(msg))
                    onResult(true, msg)
                }
            } else {
                val msg = "No retail product found with barcode: '$trimmed'"
                _eventFlow.emit(UiEvent.ShowSnackbar(msg, isError = true))
                onResult(false, msg)
            }
        }
    }

    fun addToCart(product: Product, quantityToAdd: Int = 1, batchNo: String = product.batchNo) {
        val currentList = _posCart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = currentList[index]
            val newQty = (existing.quantity + quantityToAdd).coerceAtMost(product.quantity)
            currentList[index] = existing.copy(quantity = newQty, batchNo = batchNo.ifBlank { existing.batchNo })
        } else {
            val initialQty = quantityToAdd.coerceAtMost(product.quantity).coerceAtLeast(1)
            currentList.add(CartItem(product = product, quantity = initialQty, batchNo = batchNo.ifBlank { product.batchNo }))
        }
        _posCart.value = currentList
    }

    fun updateCartItemQuantity(productId: Long, newQuantity: Int) {
        val currentList = _posCart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            if (newQuantity <= 0) {
                currentList.removeAt(index)
            } else {
                val item = currentList[index]
                val bounded = newQuantity.coerceAtMost(item.product.quantity)
                currentList[index] = item.copy(quantity = bounded)
            }
            _posCart.value = currentList
        }
    }

    fun removeFromCart(productId: Long) {
        _posCart.value = _posCart.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _posCart.value = emptyList()
    }

    fun completePosCheckout(
        paymentMethod: String,
        taxPercent: Double = _storeSettings.value.vatRatePercent,
        discountAmount: Double,
        customerName: String,
        customerId: Long? = null,
        loyaltyPointsRedeemed: Int = 0,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val items = _posCart.value
            if (items.isEmpty()) {
                onComplete(false, "Cart is empty")
                return@launch
            }

            val result = repository.processPosCheckout(
                items = items,
                paymentMethod = paymentMethod,
                taxPercent = taxPercent,
                discountAmount = discountAmount,
                customerName = customerName,
                cashierName = developerName,
                customerId = customerId,
                loyaltyPointsRedeemed = loyaltyPointsRedeemed
            )

            result.onSuccess { transactions ->
                clearCart()
                _activeInvoicePreview.value = transactions
                val total = transactions.sumOf { it.totalAmount }
                val totalFormatted = _storeSettings.value.formatPrice(total)
                val msg = "Invoice ${transactions.firstOrNull()?.invoiceNumber ?: ""} generated for $totalFormatted"
                _eventFlow.emit(UiEvent.ShowSnackbar(msg))
                onComplete(true, msg)
            }.onFailure { err ->
                val errorMsg = err.message ?: "POS Checkout failed"
                _eventFlow.emit(UiEvent.ShowSnackbar(errorMsg, isError = true))
                onComplete(false, errorMsg)
            }
        }
    }

    fun showInvoicePreview(transactions: List<SaleTransaction>) {
        _activeInvoicePreview.value = transactions
    }

    fun dismissInvoicePreview() {
        _activeInvoicePreview.value = null
    }

    // CRUD: Create Product
    fun createProduct(
        name: String,
        sku: String,
        barcode: String = "",
        category: String,
        quantity: Int,
        price: Double,
        costPrice: Double = 0.0,
        minStockThreshold: Int = 5,
        unit: String = "Pcs",
        description: String = "",
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            if (name.isBlank()) {
                onResult(false, "Product name cannot be empty")
                return@launch
            }
            if (sku.isBlank()) {
                onResult(false, "SKU code cannot be empty")
                return@launch
            }
            val existingSku = repository.getProductBySku(sku.trim())
            if (existingSku != null) {
                onResult(false, "Product with SKU '$sku' already exists")
                return@launch
            }

            val product = Product(
                name = name.trim(),
                sku = sku.trim().uppercase(),
                barcode = barcode.trim(),
                category = category.trim().ifBlank { "General Merchandise" },
                quantity = quantity.coerceAtLeast(0),
                price = price.coerceAtLeast(0.0),
                costPrice = costPrice.coerceAtLeast(0.0),
                minStockThreshold = minStockThreshold.coerceAtLeast(1),
                unit = unit.trim().ifBlank { "Pcs" },
                description = description.trim(),
                updatedAt = System.currentTimeMillis()
            )

            try {
                repository.insertProduct(product)
                _eventFlow.emit(UiEvent.ShowSnackbar("Product '${product.name}' registered"))
                onResult(true, "Product added successfully")
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Failed to add: ${e.message}", isError = true))
                onResult(false, e.message ?: "Failed to add product")
            }
        }
    }

    // CRUD: Update Product
    fun updateProduct(
        product: Product,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                repository.updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
                _eventFlow.emit(UiEvent.ShowSnackbar("Product updated successfully"))
                onResult(true, "Product updated successfully")
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Failed to update product: ${e.message}", isError = true))
                onResult(false, e.message ?: "Failed to update product")
            }
        }
    }

    // CRUD: Delete Product
    fun deleteProduct(
        product: Product,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(product)
                _eventFlow.emit(UiEvent.ShowSnackbar("Product '${product.name}' deleted"))
                onResult(true, "Product deleted")
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Failed to delete product: ${e.message}", isError = true))
                onResult(false, e.message ?: "Failed to delete product")
            }
        }
    }

    // Quick Stock Adjustment (+ or -)
    fun adjustStock(productId: Long, delta: Int) {
        viewModelScope.launch {
            val result = repository.adjustStock(productId, delta)
            result.onSuccess { newQty ->
                val sign = if (delta > 0) "+$delta" else "$delta"
                _eventFlow.emit(UiEvent.ShowSnackbar("Stock updated ($sign). Current: $newQty units"))
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowSnackbar(err.message ?: "Could not adjust stock", isError = true))
            }
        }
    }

    // Single item quick sale
    fun recordSale(
        productId: Long,
        quantitySold: Int,
        paymentMethod: String = "Cash",
        customerName: String = "Walk-in Retail Customer",
        notes: String = "",
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val result = repository.recordSale(productId, quantitySold, paymentMethod, customerName, notes)
            result.onSuccess { transaction ->
                val msg = "Invoice ${transaction.displayInvoiceNumber} generated! Sold ${transaction.quantitySold}x ${transaction.productName}"
                _eventFlow.emit(UiEvent.ShowSnackbar(msg))
                _activeInvoicePreview.value = listOf(transaction)
                onResult(true, msg)
            }.onFailure { err ->
                val errorMsg = err.message ?: "Sale failed"
                _eventFlow.emit(UiEvent.ShowSnackbar(errorMsg, isError = true))
                onResult(false, errorMsg)
            }
        }
    }

    // Auth and Refund/Exchange flows
    fun login(username: String, pass: String, role: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val user = repository.login(username, pass)
            if (user != null && user.role.equals(role, ignoreCase = true)) {
                _currentUser.value = user
                _storeSettings.value = _storeSettings.value.copy(
                    storeName = user.businessName
                )
                _eventFlow.emit(UiEvent.ShowSnackbar("Logged in as ${user.fullName} (${user.role})"))
                onResult(true, "Success")
            } else {
                val err = "Invalid credentials or role mismatch"
                _eventFlow.emit(UiEvent.ShowSnackbar(err, isError = true))
                onResult(false, err)
            }
        }
    }

    fun registerUser(user: UserAccount, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val result = repository.registerUser(user)
            result.onSuccess { registered ->
                _currentUser.value = registered
                _storeSettings.value = _storeSettings.value.copy(
                    storeName = registered.businessName
                )
                _eventFlow.emit(UiEvent.ShowSnackbar("Business '${registered.businessName}' registered!"))
                onResult(true, "Success")
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowSnackbar(err.message ?: "Sign up failed", isError = true))
                onResult(false, err.message ?: "Sign up failed")
            }
        }
    }

    fun logout() {
        _currentUser.value = UserAccount(username = "guest", password = "", fullName = "Guest Cashier", role = "CASHIER", businessName = "WSH Corporates Retail Store")
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar("Logged out"))
        }
    }

    fun processRefund(transactionId: Long, quantity: Int, reason: String) {
        viewModelScope.launch {
            val result = repository.processRefund(transactionId, quantity, reason, _currentUser.value.fullName)
            result.onSuccess { record ->
                _eventFlow.emit(UiEvent.ShowSnackbar("Refund verified! Credit Note generated: ${record.displayRecordNumber}"))
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowSnackbar(err.message ?: "Refund failed", isError = true))
            }
        }
    }

    fun processExchange(originalTxId: Long, returnQuantity: Int, newProductId: Long, newQuantity: Int, reason: String) {
        viewModelScope.launch {
            val result = repository.processExchange(originalTxId, returnQuantity, newProductId, newQuantity, _currentUser.value.fullName, reason)
            result.onSuccess { record ->
                _eventFlow.emit(UiEvent.ShowSnackbar("Exchange completed! Exchange receipt: ${record.displayRecordNumber}"))
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowSnackbar(err.message ?: "Exchange failed", isError = true))
            }
        }
    }

    private val _currentBranchName = MutableStateFlow("Olaya, Riyadh HQ")
    val currentBranchName: StateFlow<String> = _currentBranchName.asStateFlow()

    fun setBranch(branchName: String) {
        _currentBranchName.value = branchName
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar("Switched active terminal to: $branchName"))
        }
    }

    fun dispatchStockToBranch(productId: Long, qty: Int, targetBranch: String) {
        viewModelScope.launch {
            val result = repository.adjustStock(productId, -qty)
            result.onSuccess {
                _eventFlow.emit(UiEvent.ShowSnackbar("Dispatched $qty units from Main Warehouse to $targetBranch"))
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowSnackbar("Dispatch failed: ${err.message}", isError = true))
            }
        }
    }

    fun createCustomer(name: String, phone: String, email: String = "", onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            if (name.isBlank() || phone.isBlank()) {
                onResult(false, "Customer name and mobile number are required")
                return@launch
            }
            val customer = CustomerProfile(name = name.trim(), phone = phone.trim(), email = email.trim())
            val id = repository.insertCustomer(customer)
            if (id > 0) {
                _eventFlow.emit(UiEvent.ShowSnackbar("CRM Profile created for ${customer.name} • 0 points"))
                onResult(true, "Customer successfully registered")
            } else {
                onResult(false, "Failed to register customer")
            }
        }
    }

    fun updateCustomer(customer: CustomerProfile) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
            _eventFlow.emit(UiEvent.ShowSnackbar("CRM Customer Profile updated"))
        }
    }

    fun deleteCustomer(customer: CustomerProfile) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            _eventFlow.emit(UiEvent.ShowSnackbar("CRM Customer profile removed"))
        }
    }

    fun generatePurchaseOrder(supplierName: String, poText: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            if (supplierName.isBlank()) {
                onResult(false, "Supplier name cannot be empty")
                return@launch
            }
            val poNum = "PO-WSH-${1000 + (System.currentTimeMillis() % 9000)}"
            val msg = "ERP: Purchase Order $poNum generated & queued for dispatch to $supplierName!"
            _eventFlow.emit(UiEvent.ShowSnackbar(msg))
            onResult(true, poNum)
        }
    }

    // Reset to retail demo data
    fun resetToDemoData() {
        viewModelScope.launch {
            repository.resetToSampleData()
            clearCart()
            _eventFlow.emit(UiEvent.ShowSnackbar("Retail inventory & invoices restored"))
        }
    }
}

class InventoryViewModelFactory(
    private val repository: InventoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
