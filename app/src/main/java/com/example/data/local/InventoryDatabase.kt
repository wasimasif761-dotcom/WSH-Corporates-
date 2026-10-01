package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Product
import com.example.data.model.RefundRecord
import com.example.data.model.SaleTransaction
import com.example.data.model.UserAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Product::class, SaleTransaction::class, UserAccount::class, RefundRecord::class],
    version = 2,
    exportSchema = false
)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userDao(): UserDao
    abstract fun refundDao(): RefundDao

    companion object {
        @Volatile
        private var INSTANCE: InventoryDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): InventoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InventoryDatabase::class.java,
                    "wsh_corporates_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(InventoryDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getSampleProducts(): List<Product> {
            val now = System.currentTimeMillis()
            val oneDay = 24L * 3600 * 1000
            return listOf(
                Product(
                    name = "Basmati Super Kernel Rice (5 Kg)",
                    sku = "GRO-RICE-BAS5",
                    barcode = "8964000301124",
                    category = "Groceries & FMCG",
                    quantity = 24,
                    price = 45.00,
                    costPrice = 34.50,
                    minStockThreshold = 8,
                    unit = "Bag",
                    description = "Premium aged long grain aromatic basmati rice.",
                    expiryDate = now + oneDay * 365, // 1 year
                    updatedAt = now - 3600000 * 2
                ),
                Product(
                    name = "Coca-Cola Original Taste 500ml",
                    sku = "BEV-COKE-500",
                    barcode = "5449000000996",
                    category = "Beverages & Cold Drinks",
                    quantity = 48,
                    price = 3.50,
                    costPrice = 2.20,
                    minStockThreshold = 15,
                    unit = "Bottle",
                    description = "Refreshing chilled soft drink beverage.",
                    expiryDate = now + oneDay * 180, // 6 months
                    updatedAt = now - 3600000 * 5
                ),
                Product(
                    name = "Lays Classic Salted Potato Chips 65g",
                    sku = "SNK-LAYS-CLS",
                    barcode = "8901491101837",
                    category = "Snacks & Confectionery",
                    quantity = 60,
                    price = 4.00,
                    costPrice = 2.50,
                    minStockThreshold = 20,
                    unit = "Pack",
                    description = "Crispy golden salted farm-grown potato chips.",
                    expiryDate = now + oneDay * 90, // 3 months
                    updatedAt = now - 3600000 * 8
                ),
                Product(
                    name = "Nestle Nido Full Cream Milk Powder 400g",
                    sku = "MLK-NID-400",
                    barcode = "7613035391012",
                    category = "Dairy & Bakery",
                    quantity = 32,
                    price = 28.00,
                    costPrice = 21.50,
                    minStockThreshold = 10,
                    unit = "Tin",
                    description = "Fortified instant milk powder rich in calcium and vitamins.",
                    expiryDate = now + oneDay * 240, // 8 months
                    updatedAt = now - 3600000 * 12
                ),
                Product(
                    name = "Fresh Organic Laban Drink 1L",
                    sku = "DRY-LBN-1L",
                    barcode = "6281007000124",
                    category = "Dairy & Bakery",
                    quantity = 18,
                    price = 5.50,
                    costPrice = 3.80,
                    minStockThreshold = 10,
                    unit = "Bottle",
                    description = "Fresh probiotic cultured laban drink.",
                    expiryDate = now + oneDay * 14, // EXPIRING SOON (14 days alert!)
                    updatedAt = now - 3600000 * 14
                ),
                Product(
                    name = "Head & Shoulders Smooth & Silky 360ml",
                    sku = "HSC-SHMP-360",
                    barcode = "4902430751933",
                    category = "Personal Hygiene & Care",
                    quantity = 28,
                    price = 19.50,
                    costPrice = 14.20,
                    minStockThreshold = 8,
                    unit = "Bottle",
                    description = "Anti-dandruff daily care shampoo with moisturizers.",
                    expiryDate = now + oneDay * 500,
                    updatedAt = now - 3600000 * 16
                ),
                Product(
                    name = "Dettol Antiseptic Disinfectant Liquid 500ml",
                    sku = "CLH-DET-500",
                    barcode = "5000158068710",
                    category = "Household & Cleaning",
                    quantity = 19,
                    price = 22.00,
                    costPrice = 16.50,
                    minStockThreshold = 6,
                    unit = "Bottle",
                    description = "Multi-surface antiseptic liquid disinfectant protection.",
                    expiryDate = now + oneDay * 400,
                    updatedAt = now - 3600000 * 20
                ),
                Product(
                    name = "Ariel Automatic Laundry Powder 2.5kg",
                    sku = "CLH-ARL-250",
                    barcode = "4015600201017",
                    category = "Household & Cleaning",
                    quantity = 4, // Low stock demo!
                    price = 38.00,
                    costPrice = 29.00,
                    minStockThreshold = 8,
                    unit = "Box",
                    description = "Deep clean stain removal detergent powder.",
                    expiryDate = now + oneDay * 700,
                    updatedAt = now - 3600000 * 24
                ),
                Product(
                    name = "Anker USB-C Fast Braided Cable (1m)",
                    sku = "ELC-ANK-CBL",
                    barcode = "848061019024",
                    category = "Electronics & Accessories",
                    quantity = 15,
                    price = 35.00,
                    costPrice = 20.00,
                    minStockThreshold = 5,
                    unit = "Pcs",
                    description = "Durable 60W USB-C charging & sync cable (2yr warranty).",
                    expiryDate = 0L, // No expiry for electronics
                    updatedAt = now - 3600000 * 30
                )
            )
        }

        fun getSampleUsers(): List<UserAccount> {
            return listOf(
                UserAccount(
                    id = 1,
                    username = "waseem",
                    password = "123",
                    fullName = "Waseem (Lead POS Developer & System Admin)",
                    role = "ADMIN",
                    businessName = "WSH Corporates Retail Store",
                    phone = "+966 11 456 7890"
                ),
                UserAccount(
                    id = 2,
                    username = "cashier1",
                    password = "123",
                    fullName = "Retail Cashier 01",
                    role = "CASHIER",
                    businessName = "WSH Corporates Retail Store",
                    phone = "+966 50 123 4567"
                )
            )
        }
    }

    private class InventoryDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.productDao(), database.transactionDao(), database.userDao())
                }
            }
        }
    }
}

private suspend fun populateInitialData(
    productDao: ProductDao,
    transactionDao: TransactionDao,
    userDao: UserDao
) {
    if (productDao.getProductCount() == 0) {
        val sampleProducts = InventoryDatabase.getSampleProducts()
        sampleProducts.forEach { product ->
            productDao.insertProduct(product)
        }
    }

    if (userDao.getUserCount() == 0) {
        InventoryDatabase.getSampleUsers().forEach { user ->
            userDao.insertUser(user)
        }
    }

    if (transactionDao.getTransactionCount() == 0) {
        val now = System.currentTimeMillis()
        val oneHour = 3600000L

        val initialTransactions = listOf(
            SaleTransaction(
                id = 1,
                invoiceNumber = "WSH-POS-1001",
                productId = 1,
                productName = "Basmati Super Kernel Rice (5 Kg)",
                productSku = "GRO-RICE-BAS5",
                productBarcode = "8964000301124",
                quantitySold = 2,
                unitPrice = 45.00,
                subtotal = 90.00,
                taxAmount = 13.50, // 15% KSA VAT
                discountAmount = 0.0,
                totalAmount = 103.50,
                paymentMethod = "POS Card Terminal",
                cashierName = "Waseem",
                customerName = "Ahmed Al-Harbi",
                timestamp = now - oneHour * 3,
                notes = "ZATCA e-invoice verified",
                businessName = "WSH Corporates Retail Store"
            ),
            SaleTransaction(
                id = 2,
                invoiceNumber = "WSH-POS-1002",
                productId = 4,
                productName = "Nestle Nido Full Cream Milk Powder 400g",
                productSku = "MLK-NID-400",
                productBarcode = "7613035391012",
                quantitySold = 1,
                unitPrice = 28.00,
                subtotal = 28.00,
                taxAmount = 4.20,
                discountAmount = 0.0,
                totalAmount = 32.20,
                paymentMethod = "Cash",
                cashierName = "Waseem",
                customerName = "Fatima Al-Otaibi",
                timestamp = now - oneHour * 2,
                notes = "Cash tendered 50 SAR",
                businessName = "WSH Corporates Retail Store"
            )
        )

        initialTransactions.forEach { tx ->
            transactionDao.insertTransaction(tx)
        }
    }
}
