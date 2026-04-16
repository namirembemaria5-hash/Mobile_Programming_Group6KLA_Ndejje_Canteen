package com.ndejje.ndejjecanteen.utils

import com.ndejje.ndejjecanteen.data.model.MenuItem
import java.text.NumberFormat
import java.util.*

fun formatUGX(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "UG"))
    format.currency = Currency.getInstance("UGX")
    format.maximumFractionDigits = 0
    return format.format(amount).replace("UGX", "UGX ")
}

fun isWeekend(): Boolean {
    val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    return day == Calendar.SATURDAY || day == Calendar.SUNDAY
}

fun getItemEmoji(item: MenuItem): String {
    return when {
        item.name.contains("Mandaazi", ignoreCase = true) -> "🍩"
        item.name.contains("Samosa", ignoreCase = true) -> "🥟"
        item.name.contains("Cassava", ignoreCase = true) -> "🍟"
        item.name.contains("Kikomando", ignoreCase = true) -> "🫓"
        item.name.contains("Water", ignoreCase = true) -> "💧"
        item.name.contains("Energy", ignoreCase = true) || item.name.contains("Bull", ignoreCase = true) || item.name.contains("Monster", ignoreCase = true) || item.name.contains("Sting", ignoreCase = true) -> "⚡"
        item.name.contains("Juice", ignoreCase = true) -> "🍹"
        item.name.contains("Soda", ignoreCase = true) || item.name.contains("Cola", ignoreCase = true) || item.name.contains("Pepsi", ignoreCase = true) || item.name.contains("Mirinda", ignoreCase = true) || item.name.contains("Sprite", ignoreCase = true) -> "🥤"
        item.name.contains("Coffee", ignoreCase = true) -> "☕"
        item.name.contains("Tea", ignoreCase = true) -> "🍵"
        item.name.contains("Rice", ignoreCase = true) -> "🍚"
        item.name.contains("Matooke", ignoreCase = true) -> "🍌"
        item.name.contains("Posho", ignoreCase = true) -> "🫓"
        item.name.contains("Chapati", ignoreCase = true) -> "🫓"
        item.name.contains("Beef", ignoreCase = true) -> "🥩"
        item.name.contains("Chicken", ignoreCase = true) -> "🍗"
        item.name.contains("Beans", ignoreCase = true) -> "🫘"
        item.name.contains("Egg", ignoreCase = true) -> "🍳"
        item.name.contains("Lusaniya", ignoreCase = true) -> "🔥"
        item.name.contains("Pasta", ignoreCase = true) -> "🍝"
        else -> "🍽️"
    }
}
