package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SaleTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM sale_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<SaleTransaction>>

    @Query("SELECT * FROM sale_transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<SaleTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: SaleTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<SaleTransaction>): List<Long>

    @Delete
    suspend fun deleteTransaction(transaction: SaleTransaction)

    @Query("SELECT COUNT(*) FROM sale_transactions")
    suspend fun getTransactionCount(): Int

    @Query("SELECT * FROM sale_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): SaleTransaction?

    @androidx.room.Update
    suspend fun updateTransaction(transaction: SaleTransaction)

    @Query("SELECT SUM(totalAmount) FROM sale_transactions")
    fun getTotalRevenue(): Flow<Double?>
}
