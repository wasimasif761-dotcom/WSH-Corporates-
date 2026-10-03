package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomerProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customer_profiles ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerProfile>>

    @Query("SELECT * FROM customer_profiles WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerProfile?

    @Query("SELECT * FROM customer_profiles WHERE phone = :phone LIMIT 1")
    suspend fun getCustomerByPhone(phone: String): CustomerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerProfile>)

    @Query("SELECT COUNT(*) FROM customer_profiles")
    suspend fun getCustomerCount(): Int

    @Update
    suspend fun updateCustomer(customer: CustomerProfile)

    @Delete
    suspend fun deleteCustomer(customer: CustomerProfile)

    @Query("UPDATE customer_profiles SET loyaltyPoints = :points WHERE id = :id")
    suspend fun updateLoyaltyPoints(id: Long, points: Int)
}
