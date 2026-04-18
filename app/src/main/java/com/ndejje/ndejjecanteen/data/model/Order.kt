package com.ndejje.ndejjecanteen.data.model

import com.google.firebase.firestore.PropertyName

enum class OrderStatus(val displayName: String, val emoji: String) {
    PENDING("Order Placed", "⏳"),
    PREPARING("Preparing", "👨‍🍳"),
    READY("Ready", "✅"),
    IN_TRANSIT("In Transit", "🚚"),
    DELIVERED("Delivered", "🏁"),
    CANCELLED("Cancelled", "❌")
}

enum class PaymentMethod(val displayName: String, val emoji: String) {
    CASH("Cash on Delivery", "💵"),
    AIRTEL_MONEY("Airtel Money", "🔴"),
    MTN_MOMO("MTN MoMo", "🟡")
}

enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED
}

data class OrderLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = ""
)

data class OrderItem(
    val itemId: String = "",
    val itemName: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val subtotal: Double = 0.0
)

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = OrderStatus.PENDING.name,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val location: OrderLocation? = null,
    
    @get:PropertyName("isPreOrder")
    @set:PropertyName("isPreOrder")
    var isPreOrder: Boolean = false,

    val preOrderDate: String = "",
    val notes: String = "",
    val paymentMethod: String = PaymentMethod.CASH.name,
    val paymentStatus: String = PaymentStatus.PENDING.name,
    val deliveryPersonId: String? = null,
    val deliveryPersonName: String? = null
) {
    constructor() : this("", "", "", "", emptyList(), 0.0, OrderStatus.PENDING.name, System.currentTimeMillis(), System.currentTimeMillis(), null, false, "", "", PaymentMethod.CASH.name, PaymentStatus.PENDING.name, null, null)
}
