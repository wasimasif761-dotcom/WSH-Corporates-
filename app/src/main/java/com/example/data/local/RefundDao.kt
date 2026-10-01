package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.RefundRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface RefundDao {
    @Query("SELECT * FROM refund_records ORDER BY timestamp DESC")
    fun getAllRefundRecords(): Flow<List<RefundRecord>>

    @Query("SELECT * FROM refund_records WHERE originalInvoiceNumber = :invoiceNumber")
    suspend fun getRecordsByInvoice(invoiceNumber: String): List<RefundRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRefundRecord(record: RefundRecord): Long
}
