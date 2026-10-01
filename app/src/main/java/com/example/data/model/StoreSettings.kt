package com.example.data.model

data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val isPrefix: Boolean = true
)

object SupportedCurrencies {
    val ALL = listOf(
        Currency("SAR", "Saudi Riyal (المملكة العربية السعودية)", "SAR", isPrefix = false),
        Currency("USD", "US Dollar ($)", "$", isPrefix = true),
        Currency("EUR", "Euro (€)", "€", isPrefix = true),
        Currency("GBP", "British Pound (£)", "£", isPrefix = true),
        Currency("AED", "UAE Dirham (الإمارات)", "AED", isPrefix = false),
        Currency("KWD", "Kuwaiti Dinar (الكويت)", "KWD", isPrefix = false),
        Currency("PKR", "Pakistani Rupee (Rs)", "Rs.", isPrefix = true),
        Currency("INR", "Indian Rupee (₹)", "₹", isPrefix = true)
    )

    fun getByCode(code: String): Currency {
        return ALL.find { it.code.equals(code, ignoreCase = true) } ?: ALL.first()
    }
}

data class StoreSettings(
    val storeName: String = "WSH Corporates Retail Store",
    val branchName: String = "Olaya District, Riyadh, Saudi Arabia",
    val taxRegistrationNumber: String = "310123456700003", // 15-digit KSA ZATCA VAT ID
    val commercialRegistration: String = "1010892741", // KSA CR Number
    val phone: String = "+966 11 456 7890",
    val currencyCode: String = "SAR",
    val vatRatePercent: Double = 15.0, // Saudi Arabia Standard VAT (15%)
    val showTermsOnInvoice: Boolean = true,
    val termsAndConditions: String = DEFAULT_KSA_TERMS
) {
    val currency: Currency
        get() = SupportedCurrencies.getByCode(currencyCode)

    fun formatPrice(amount: Double): String {
        val formatted = String.format(java.util.Locale.US, "%.2f", amount)
        return if (currency.isPrefix) {
            "${currency.symbol}$formatted"
        } else {
            "$formatted ${currency.symbol}"
        }
    }

    companion object {
        const val DEFAULT_KSA_TERMS =
            "1. Goods returnable within 7 days, exchangeable within 14 days with original receipt in saleable condition.\n" +
            "   (البضاعة المباعة ترد خلال ٧ أيام وتستبدل خلال ١٤ يوماً بشرط وجود الفاتورة الأصلية وبحالتها).\n" +
            "2. Fresh items, dairy, and hygiene products cannot be returned/exchanged as per health regulations.\n" +
            "   (المواد الغذائية الطازجة ومنتجات العناية الشخصية لا ترد ولا تستبدل حفاظاً على الصحة العامة).\n" +
            "3. 15% VAT included per ZATCA (Zakat, Tax and Customs Authority) Saudi Arabia regulations.\n" +
            "   (الأسعار تشمل ضريبة القيمة المضافة ١٥٪ وفقاً لهيئة الزكاة والضريبة والجمارك).\n" +
            "4. Electronics carry 2-year warranty per Ministry of Commerce guidelines.\n" +
            "   (يسري ضمان سنتين على الأجهزة الإلكترونية وفق تعليمات وزارة التجارة)."
    }
}
