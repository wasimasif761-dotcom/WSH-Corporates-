package com.example

import android.app.Application
import com.example.data.local.InventoryDatabase
import com.example.data.repository.InventoryRepository
import com.example.data.repository.InventoryRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class InventraApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: InventoryDatabase by lazy {
        InventoryDatabase.getDatabase(this, applicationScope)
    }

    val repository: InventoryRepository by lazy {
        InventoryRepositoryImpl(
            database.productDao(),
            database.transactionDao(),
            database.userDao(),
            database.refundDao()
        )
    }
}
