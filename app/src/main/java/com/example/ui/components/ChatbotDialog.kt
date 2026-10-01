package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Product
import com.example.data.model.SaleTransaction
import com.example.data.model.StoreSettings
import com.example.ui.theme.CorporateBlue
import com.example.ui.theme.EmeraldSuccess
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER,
    BOT
}

@Composable
fun ChatbotDialog(
    products: List<Product>,
    transactions: List<SaleTransaction>,
    storeSettings: StoreSettings,
    currentUserRole: String,
    developerName: String = "Waseem",
    onDismiss: () -> Unit
) {
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = MessageSender.BOT,
                text = "Hello! I am your WSH Retail AI Assistant, developed by $developerName.\n" +
                    "مرحباً بك! أنا مساعدك الذكي لنقاط البيع وإدارة المتجر، طُوِّر بواسطة $developerName.\n\n" +
                    "Ask me anything like:\n" +
                    "• 'How many sales today?' / 'كم مبيعات اليوم؟'\n" +
                    "• 'Which items are expiring soon?' / 'ما المنتجات قريبة الانتهاء؟'\n" +
                    "• 'What are the low stock items?' / 'ما هي نواقص الرفوف؟'\n" +
                    "• 'What is our refund/exchange policy?' / 'شروط الاسترجاع والاستبدال'"
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    fun respondToQuery(userQuery: String) {
        val q = userQuery.lowercase().trim()
        val isArabic = q.any { it in '\u0600'..'\u06FF' }

        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = todayCal.timeInMillis
        val todayTransactions = transactions.filter { it.timestamp >= startOfToday }
        val todaySalesCount = todayTransactions.size
        val todayRevenue = todayTransactions.sumOf { it.totalAmount }
        val totalRevenue = transactions.sumOf { it.totalAmount }

        val expiringItems = products.filter { it.isExpiringSoon || it.isExpired }
        val lowStockItems = products.filter { it.isLowStock || it.isOutOfStock }

        val reply = when {
            // Sales Today Query
            q.contains("sale") || q.contains("today") || q.contains("مبيع") || q.contains("اليوم") || q.contains("دخل") -> {
                if (isArabic) {
                    "📊 تقرير مبيعات اليوم:\n" +
                        "• عدد الفواتير اليوم: $todaySalesCount فاتورة\n" +
                        "• إجمالي إيرادات اليوم: ${storeSettings.formatPrice(todayRevenue)}\n" +
                        "• إجمالي كل المبيعات المسجلة: ${storeSettings.formatPrice(totalRevenue)} من ${transactions.size} فاتورة."
                } else {
                    "📊 Today's Sales Performance:\n" +
                        "• Invoices Today: $todaySalesCount invoices\n" +
                        "• Today's Gross Revenue: ${storeSettings.formatPrice(todayRevenue)}\n" +
                        "• All-Time Total Revenue: ${storeSettings.formatPrice(totalRevenue)} (${transactions.size} total invoices)."
                }
            }

            // Expiry Date Query
            q.contains("expir") || q.contains("صلاحي") || q.contains("انتهاء") || q.contains("تاريخ") -> {
                if (expiringItems.isEmpty()) {
                    if (isArabic) "✅ جميع المنتجات على الرفوف صالحة ولا توجد أي منتجات تنتهي صلاحيتها خلال ٣٠ يوماً!"
                    else "✅ Great news! All shelf items have healthy shelf lives. No items expiring within 30 days."
                } else {
                    val listStr = expiringItems.joinToString("\n") { p ->
                        "• ${p.name}: ${p.formattedExpiryDate} (${p.quantity} ${p.unit} left)"
                    }
                    if (isArabic) {
                        "⚠️ منتجات قريبة من انتهاء الصلاحية أو منتهية (${expiringItems.size} أصناف):\n$listStr\n\nنوصي بعمل خصم ترويجي لتصريفها سريعاً!"
                    } else {
                        "⚠️ Shelf Life Alert (${expiringItems.size} items):\n$listStr\n\nTip: You can create a promo offer to clear these items quickly!"
                    }
                }
            }

            // Low Stock Query
            q.contains("stock") || q.contains("low") || q.contains("مخزون") || q.contains("نواقص") || q.contains("كمي") -> {
                if (lowStockItems.isEmpty()) {
                    if (isArabic) "📦 الرفوف ممتلئة ولا يوجد أي صنف أقل من الحد الأدنى!"
                    else "📦 All inventory stocks are healthy. No products under the alert threshold."
                } else {
                    val listStr = lowStockItems.take(5).joinToString("\n") { p ->
                        "• ${p.name}: ${p.quantity} ${p.unit} remaining (Min: ${p.minStockThreshold})"
                    }
                    if (isArabic) {
                        "⚠️ تنبيه نواقص المخزون (${lowStockItems.size} أصناف):\n$listStr"
                    } else {
                        "⚠️ Low Stock Alert (${lowStockItems.size} items):\n$listStr"
                    }
                }
            }

            // Refund & Exchange Query
            q.contains("refund") || q.contains("exchange") || q.contains("return") || q.contains("استرجاع") || q.contains("استبدال") || q.contains("ترجيع") -> {
                if (isArabic) {
                    "🔄 سياسة الاسترجاع والاستبدال (المملكة العربية السعودية):\n" +
                        "١. البضاعة المباعة ترد خلال ٧ أيام وتستبدل خلال ١٤ يوماً مع إحضار الفاتورة الأصلية وبحالتها.\n" +
                        "٢. المنتجات الغذائية الطازجة والعناية الشخصية لا ترد ولا تستبدل حفاظاً على الصحة العامة.\n" +
                        "٣. لمعالجة استرجاع، افتح قائمة الفواتير واضغط على خيار (استرجاع / استبدال) وسيتم إعادة البضاعة للمخزن آلياً!"
                } else {
                    "🔄 Refund & Exchange Policy (KSA Regulations):\n" +
                        "1. Return within 7 days; exchange within 14 days with original receipt in saleable condition.\n" +
                        "2. Fresh items, dairy, and hygiene products cannot be returned/exchanged.\n" +
                        "3. To process a return, open the Invoices screen, tap the transaction, and select 'Refund or Exchange'. Stock will be automatically restored!"
                }
            }

            // Who developed this software / Brand ownership
            q.contains("who") || q.contains("waseem") || q.contains("developer") || q.contains("owner") || q.contains("مين") || q.contains("صانع") || q.contains("وسيم") -> {
                if (isArabic) {
                    "🌟 هذا النظام صُمم وطُوِّر بواسطة المهندس وسيم (WSH Corporates). كل شركة أو متجر يمكنها تخصيص اسمها وفواتيرها وعملتها، ولكن ملكية وتطوير المحرك البرمجي يظل فخراً للمطور وسيم."
                } else {
                    "🌟 This retail POS software engine was engineered & developed with pride by Waseem (WSH Corporates). Any retail store or enterprise can brand their own store and tax invoices, powered by Waseem's retail platform!"
                }
            }

            // Valuation
            q.contains("value") || q.contains("worth") || q.contains("قيمة") || q.contains("ثمن") -> {
                val totalVal = products.sumOf { it.totalValue }
                if (isArabic) {
                    "💰 القيمة الإجمالية للمخزون الحالي بالرفوف: ${storeSettings.formatPrice(totalVal)} تشمل ${products.size} صنفاً بإجمالي ${products.sumOf { it.quantity }} وحدة."
                } else {
                    "💰 Total Inventory Asset Valuation: ${storeSettings.formatPrice(totalVal)} across ${products.size} retail SKUs (${products.sumOf { it.quantity }} units)."
                }
            }

            // Default fallback
            else -> {
                if (isArabic) {
                    "شكراً لسؤالك! أنا مساعدك الذكي في ${storeSettings.storeName}. يمكنني مساعدتك في: مبيعات اليوم، نواقص المخزون، تواريخ الصلاحية، سياسة الاسترجاع، وطرق استخدام النظام."
                } else {
                    "I am here to assist your retail operations! Ask me about: today's sales, expiring dates, low stock items, refunds/exchange policy, or store valuation."
                }
            }
        }

        messages.add(ChatMessage(sender = MessageSender.BOT, text = reply))
        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(580.dp)
                .padding(vertical = 12.dp)
                .testTag("chatbot_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CorporateBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "WSH Retail AI Assistant",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "مساعد وسيم الذكي للمبيعات (English & Arabic)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close chat")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Prompt Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val promptList = listOf(
                        "Sales Today (مبيعات اليوم)" to "How many sales today?",
                        "Expiring Soon (تواريخ الصلاحية)" to "Which items are expiring soon?",
                        "Low Stock (نواقص المخزون)" to "What are the low stock items?",
                        "Refund Policy (الاسترجاع)" to "What is our refund and exchange policy?",
                        "Store Valuation (قيمة البضاعة)" to "Total store inventory valuation"
                    )

                    promptList.forEach { (label, prompt) ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                messages.add(ChatMessage(sender = MessageSender.USER, text = prompt))
                                respondToQuery(prompt)
                            },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isUser = msg.sender == MessageSender.USER
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            if (!isUser) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(CorporateBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = CorporateBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) CorporateBlue else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.widthIn(max = 290.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    ),
                                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            if (isUser) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask in English or بالعربية...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onDone = {
                            if (inputText.isNotBlank()) {
                                val text = inputText.trim()
                                inputText = ""
                                messages.add(ChatMessage(sender = MessageSender.USER, text = text))
                                respondToQuery(text)
                            }
                        }),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chatbot_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val text = inputText.trim()
                                inputText = ""
                                messages.add(ChatMessage(sender = MessageSender.USER, text = text))
                                respondToQuery(text)
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(CorporateBlue)
                            .testTag("chatbot_send_btn")
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
