package com.example.data.model

data class DiscountOffer(
    val id: String,
    val code: String,
    val title: String,
    val discountPercent: Double = 0.0,
    val flatDiscount: Double = 0.0,
    val minOrderAmount: Double = 0.0,
    val description: String = ""
) {
    fun calculateDiscount(subtotal: Double): Double {
        if (subtotal < minOrderAmount) return 0.0
        val percentPart = if (discountPercent > 0) subtotal * (discountPercent / 100.0) else 0.0
        val total = percentPart + flatDiscount
        return total.coerceAtMost(subtotal)
    }

    companion object {
        val DEFAULT_OFFERS = listOf(
            DiscountOffer(
                id = "eid15",
                code = "EID15",
                title = "Saudi Season Promo",
                discountPercent = 15.0,
                minOrderAmount = 50.0,
                description = "15% off orders above 50 SAR"
            ),
            DiscountOffer(
                id = "wsh10",
                code = "WSH10",
                title = "WSH Corporate Member",
                discountPercent = 10.0,
                minOrderAmount = 20.0,
                description = "10% VIP corporate discount"
            ),
            DiscountOffer(
                id = "save5",
                code = "SAVE5",
                title = "Flat 5 Discount",
                flatDiscount = 5.0,
                minOrderAmount = 25.0,
                description = "Flat 5 off on minimum 25 purchase"
            ),
            DiscountOffer(
                id = "mega20",
                code = "MEGA20",
                title = "Retail Super Weekend",
                discountPercent = 20.0,
                minOrderAmount = 100.0,
                description = "20% off for retail purchases over 100"
            )
        )
    }
}
