package com.example.data.repository

import com.example.data.local.CustomerDao
import com.example.data.local.InventoryDatabase
import com.example.data.local.ProductDao
import com.example.data.local.RefundDao
import com.example.data.local.TransactionDao
import com.example.data.local.UserDao
import com.example.data.model.CustomerProfile
import com.example.data.model.Product
import com.example.data.model.RefundRecord
import com.example.data.model.SaleTransaction
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val subtotal: Double get() = product.price * quantity
}

interface InventoryRepository {
    val allProducts: Flow<List<Product>>
    val allTransactions: Flow<List<SaleTransaction>>
    val recentTransactions: Flow<List<SaleTransaction>>
    val lowStockProducts: Flow<List<Product>>
    val totalRevenue: Flow<Double?>
    val allUsers: Flow<List<UserAccount>>
    val allRefunds: Flow<List<RefundRecord>>

    fun searchProducts(query: String): Flow<List<Product>>
    fun getProductsByCategory(category: String): Flow<List<Product>>
    suspend fun getProductById(id: Long): Product?
    suspend fun getProductBySku(sku: String): Product?
    suspend fun getProductByBarcodeOrSku(code: String): Product?
    suspend fun insertProduct(product: Product): Long
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
    suspend fun adjustStock(productId: Long, delta: Int): Result<Int>
    suspend fun recordSale(
        productId: Long,
        quantitySold: Int,
        paymentMethod: String = "Cash",
        customerName: String = "Walk-in Retail Customer",
        notes: String = ""
    ): Result<SaleTransaction>
    suspend fun processPosCheckout(
        items: List<CartItem>,
        paymentMethod: String = "Cash",
        taxPercent: Double = 15.0,
        discountAmount: Double = 0.0,
        customerName: String = "Walk-in Retail Customer",
        cashierName: String = "Waseem",
        customerId: Long? = null,
        loyaltyPointsRedeemed: Int = 0
    ): Result<List<SaleTransaction>>
    val allCustomers: Flow<List<CustomerProfile>>
    suspend fun insertCustomer(customer: CustomerProfile): Long
    suspend fun updateCustomer(customer: CustomerProfile)
    suspend fun deleteCustomer(customer: CustomerProfile)
    suspend fun updateCustomerLoyaltyPoints(customerId: Long, points: Int)
    suspend fun login(username: String, password: String): UserAccount?
    suspend fun registerUser(user: UserAccount): Result<UserAccount>
    suspend fun processRefund(
        transactionId: Long,
        quantity: Int,
        reason: String,
        cashierName: String
    ): Result<RefundRecord>
    suspend fun processExchange(
        originalTxId: Long,
        returnQuantity: Int,
        newProductId: Long,
        newQuantity: Int,
        cashierName: String,
        reason: String
    ): Result<RefundRecord>
    suspend fun resetToSampleData()
}

class InventoryRepositoryImpl(
    private val productDao: ProductDao,
    private val transactionDao: TransactionDao,
    private val userDao: UserDao,
    private val refundDao: RefundDao,
    private val customerDao: CustomerDao
) : InventoryRepository {

    override val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    override val allTransactions: Flow<List<SaleTransaction>> = transactionDao.getAllTransactions()
    override val recentTransactions: Flow<List<SaleTransaction>> = transactionDao.getRecentTransactions(5)
    override val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()
    override val totalRevenue: Flow<Double?> = transactionDao.getTotalRevenue()
    override val allUsers: Flow<List<UserAccount>> = userDao.getAllUsers()
    override val allRefunds: Flow<List<RefundRecord>> = refundDao.getAllRefundRecords()
    override val allCustomers: Flow<List<CustomerProfile>> = customerDao.getAllCustomers()

    override fun searchProducts(query: String): Flow<List<Product>> {
        return if (query.isBlank()) {
            productDao.getAllProducts()
        } else {
            productDao.searchProducts(query.trim())
        }
    }

    override fun getProductsByCategory(category: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(category)
    }

    override suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)

    override suspend fun getProductBySku(sku: String): Product? = productDao.getProductBySku(sku)

    override suspend fun getProductByBarcodeOrSku(code: String): Product? = productDao.getProductByBarcodeOrSku(code)

    override suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)

    override suspend fun updateProduct(product: Product) = productDao.updateProduct(product)

    override suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    override suspend fun adjustStock(productId: Long, delta: Int): Result<Int> {
        val product = productDao.getProductById(productId)
            ?: return Result.failure(IllegalArgumentException("Product not found"))

        val newQuantity = product.quantity + delta
        if (newQuantity < 0) {
            return Result.failure(IllegalStateException("Stock cannot be negative. Current: ${product.quantity}"))
        }

        val updated = product.copy(quantity = newQuantity, updatedAt = System.currentTimeMillis())
        productDao.updateProduct(updated)
        return Result.success(newQuantity)
    }

    override suspend fun recordSale(
        productId: Long,
        quantitySold: Int,
        paymentMethod: String,
        customerName: String,
        notes: String
    ): Result<SaleTransaction> {
        if (quantitySold <= 0) {
            return Result.failure(IllegalArgumentException("Quantity must be at least 1"))
        }

        val product = productDao.getProductById(productId)
            ?: return Result.failure(IllegalArgumentException("Product not found"))

        if (product.quantity < quantitySold) {
            return Result.failure(
                IllegalStateException("Insufficient stock. Available: ${product.quantity}, Requested: $quantitySold")
            )
        }

        // Deduct quantity from product stock
        val updatedProduct = product.copy(
            quantity = product.quantity - quantitySold,
            updatedAt = System.currentTimeMillis()
        )
        productDao.updateProduct(updatedProduct)

        val unitPrice = product.price
        val subtotal = unitPrice * quantitySold
        val taxRate = 15.0 // Saudi Arabia 15% standard VAT
        val taxAmount = subtotal * (taxRate / 100.0)
        val totalAmount = subtotal + taxAmount

        val uniqueInvoiceNum = "WSH-POS-${1000 + System.currentTimeMillis() % 9000}"

        val transaction = SaleTransaction(
            invoiceNumber = uniqueInvoiceNum,
            productId = product.id,
            productName = product.name,
            productSku = product.sku,
            productBarcode = product.barcode,
            quantitySold = quantitySold,
            unitPrice = unitPrice,
            subtotal = subtotal,
            taxAmount = taxAmount,
            discountAmount = 0.0,
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            customerName = customerName,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )

        val id = transactionDao.insertTransaction(transaction)
        return Result.success(transaction.copy(id = id))
    }

    override suspend fun processPosCheckout(
        items: List<CartItem>,
        paymentMethod: String,
        taxPercent: Double,
        discountAmount: Double,
        customerName: String,
        cashierName: String,
        customerId: Long?,
        loyaltyPointsRedeemed: Int
    ): Result<List<SaleTransaction>> {
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cart is empty"))
        }

        // Validate stock for all items first
        for (item in items) {
            val product = productDao.getProductById(item.product.id)
                ?: return Result.failure(IllegalArgumentException("Product '${item.product.name}' no longer exists"))
            if (product.quantity < item.quantity) {
                return Result.failure(
                    IllegalStateException("Insufficient shelf stock for '${product.name}'. Available: ${product.quantity}, in cart: ${item.quantity}")
                )
            }
        }

        val uniqueInvoiceNum = "WSH-POS-${1000 + (System.currentTimeMillis() % 9000)}"
        val timestamp = System.currentTimeMillis()

        val subtotalOverall = items.sumOf { it.subtotal }

        // Update customer loyalty points in DB
        if (customerId != null) {
            val customer = customerDao.getCustomerById(customerId)
            if (customer != null) {
                val pointsAwarded = (subtotalOverall / 10.0).toInt()
                val finalPoints = (customer.loyaltyPoints - loyaltyPointsRedeemed + pointsAwarded).coerceAtLeast(0)
                customerDao.updateCustomer(customer.copy(loyaltyPoints = finalPoints))
            }
        }

        val createdTransactions = mutableListOf<SaleTransaction>()

        for (item in items) {
            val product = productDao.getProductById(item.product.id)!!

            // Deduct stock
            val updatedProduct = product.copy(
                quantity = product.quantity - item.quantity,
                updatedAt = timestamp
            )
            productDao.updateProduct(updatedProduct)

            val itemSubtotal = item.subtotal
            // Distribute discount proportionally across items
            val itemDiscountProportion = if (subtotalOverall > 0) {
                (itemSubtotal / subtotalOverall) * discountAmount
            } else 0.0

            val itemTaxable = (itemSubtotal - itemDiscountProportion).coerceAtLeast(0.0)
            val itemTax = itemTaxable * (taxPercent / 100.0)
            val itemTotal = itemTaxable + itemTax

            val tx = SaleTransaction(
                invoiceNumber = uniqueInvoiceNum,
                productId = product.id,
                productName = product.name,
                productSku = product.sku,
                productBarcode = product.barcode,
                quantitySold = item.quantity,
                unitPrice = product.price,
                subtotal = itemSubtotal,
                taxAmount = itemTax,
                discountAmount = itemDiscountProportion,
                totalAmount = itemTotal,
                paymentMethod = paymentMethod,
                cashierName = cashierName,
                customerName = customerName,
                timestamp = timestamp,
                notes = "POS Checkout"
            )

            val insertedId = transactionDao.insertTransaction(tx)
            createdTransactions.add(tx.copy(id = insertedId))
        }

        return Result.success(createdTransactions)
    }

    override suspend fun login(username: String, password: String): UserAccount? {
        return userDao.authenticate(username.trim(), password.trim())
    }

    override suspend fun registerUser(user: UserAccount): Result<UserAccount> {
        val existing = userDao.getUserByUsername(user.username.trim())
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Username '${user.username}' is already registered"))
        }
        val id = userDao.insertUser(user)
        return Result.success(user.copy(id = id))
    }

    override suspend fun processRefund(
        transactionId: Long,
        quantity: Int,
        reason: String,
        cashierName: String
    ): Result<RefundRecord> {
        val tx = transactionDao.getTransactionById(transactionId)
            ?: return Result.failure(IllegalArgumentException("Original sale transaction not found"))

        if (tx.isRefunded) {
            return Result.failure(IllegalStateException("Invoice transaction already marked as refunded"))
        }

        if (quantity <= 0 || quantity > tx.quantitySold) {
            return Result.failure(IllegalArgumentException("Invalid refund quantity. Max returnable: ${tx.quantitySold}"))
        }

        // Restore item back to inventory stock!
        val product = productDao.getProductById(tx.productId)
        if (product != null) {
            productDao.updateProduct(
                product.copy(
                    quantity = product.quantity + quantity,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        val refundTotal = (tx.totalAmount / tx.quantitySold) * quantity

        // Update transaction status
        transactionDao.updateTransaction(
            tx.copy(
                isRefunded = true,
                refundReason = reason,
                refundTimestamp = System.currentTimeMillis()
            )
        )

        // Insert Refund Record
        val record = RefundRecord(
            originalInvoiceNumber = tx.displayInvoiceNumber,
            type = "REFUND",
            productId = tx.productId,
            productName = tx.productName,
            quantity = quantity,
            unitPrice = tx.unitPrice,
            refundTotalAmount = refundTotal,
            reason = reason,
            cashierName = cashierName,
            customerName = tx.customerName,
            timestamp = System.currentTimeMillis()
        )
        val id = refundDao.insertRefundRecord(record)
        return Result.success(record.copy(id = id))
    }

    override suspend fun processExchange(
        originalTxId: Long,
        returnQuantity: Int,
        newProductId: Long,
        newQuantity: Int,
        cashierName: String,
        reason: String
    ): Result<RefundRecord> {
        val tx = transactionDao.getTransactionById(originalTxId)
            ?: return Result.failure(IllegalArgumentException("Original sale transaction not found"))

        val newProduct = productDao.getProductById(newProductId)
            ?: return Result.failure(IllegalArgumentException("Exchange replacement product not found"))

        if (newProduct.quantity < newQuantity) {
            return Result.failure(IllegalStateException("Not enough stock for replacement item '${newProduct.name}'"))
        }

        // 1. Restore returned item to stock
        val returnProduct = productDao.getProductById(tx.productId)
        if (returnProduct != null) {
            productDao.updateProduct(
                returnProduct.copy(
                    quantity = returnProduct.quantity + returnQuantity,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        // 2. Deduct exchange replacement item from stock
        productDao.updateProduct(
            newProduct.copy(
                quantity = newProduct.quantity - newQuantity,
                updatedAt = System.currentTimeMillis()
            )
        )

        val returnedValue = (tx.totalAmount / tx.quantitySold) * returnQuantity
        val newValue = newProduct.price * newQuantity * 1.15 // with 15% VAT
        val priceDifference = newValue - returnedValue

        // Update transaction
        transactionDao.updateTransaction(
            tx.copy(
                isExchange = true,
                refundReason = "Exchanged with ${newProduct.name}: $reason",
                refundTimestamp = System.currentTimeMillis()
            )
        )

        val record = RefundRecord(
            originalInvoiceNumber = tx.displayInvoiceNumber,
            type = "EXCHANGE",
            productId = tx.productId,
            productName = tx.productName,
            quantity = returnQuantity,
            unitPrice = tx.unitPrice,
            refundTotalAmount = returnedValue,
            reason = reason,
            cashierName = cashierName,
            customerName = tx.customerName,
            exchangedWithProductId = newProduct.id,
            exchangedWithProductName = newProduct.name,
            priceDifferencePaid = priceDifference,
            timestamp = System.currentTimeMillis()
        )

        val id = refundDao.insertRefundRecord(record)
        return Result.success(record.copy(id = id))
    }

    override suspend fun resetToSampleData() {
        val sampleProducts = InventoryDatabase.getSampleProducts()
        sampleProducts.forEach { product ->
            val existing = productDao.getProductBySku(product.sku)
            if (existing != null) {
                productDao.updateProduct(product.copy(id = existing.id))
            } else {
                productDao.insertProduct(product)
            }
        }
    }

    override suspend fun insertCustomer(customer: CustomerProfile): Long {
        return customerDao.insertCustomer(customer)
    }

    override suspend fun updateCustomer(customer: CustomerProfile) {
        customerDao.updateCustomer(customer)
    }

    override suspend fun deleteCustomer(customer: CustomerProfile) {
        customerDao.deleteCustomer(customer)
    }

    override suspend fun updateCustomerLoyaltyPoints(customerId: Long, points: Int) {
        customerDao.updateLoyaltyPoints(customerId, points)
    }
}
