package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String, // Unique username or email
    val password: String,
    val fullName: String,
    val role: String = "CASHIER", // "ADMIN" or "CASHIER"
    val businessName: String = "WSH Corporates Retail Store",
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val isAdmin: Boolean
        get() = role.equals("ADMIN", ignoreCase = true)
}
