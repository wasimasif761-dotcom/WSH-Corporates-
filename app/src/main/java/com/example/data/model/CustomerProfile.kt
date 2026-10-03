package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_profiles")
data class CustomerProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val loyaltyPoints: Int = 0,
    val registeredTimestamp: Long = System.currentTimeMillis()
) {
    companion object {
        val SAMPLE_CUSTOMERS = listOf(
            CustomerProfile(
                id = 1,
                name = "Ahmed Al-Farsi",
                phone = "0501234567",
                email = "ahmed.farsi@gmail.com",
                loyaltyPoints = 120
            ),
            CustomerProfile(
                id = 2,
                name = "Fatima Siddiqui",
                phone = "03214567890",
                email = "fatima.sid@yahoo.com",
                loyaltyPoints = 250
            ),
            CustomerProfile(
                id = 3,
                name = "Waseem Anwar (Lead Tester)",
                phone = "0555554321",
                email = "waseem.test@wshcorp.com",
                loyaltyPoints = 500
            )
        )
    }
}
