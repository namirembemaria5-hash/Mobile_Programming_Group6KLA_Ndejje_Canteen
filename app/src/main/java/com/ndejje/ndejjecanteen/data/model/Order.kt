package com.ndejje.ndejjecanteen.data.model

enum class OrderStatus(val displayName: String, val emoji: String) {
    PENDING("Order Placed", "⏳"),
    PREPARING("Preparing", "👨‍🍳"),
    READY("Ready", "✅"),
    CANCELLED("Cancelled", "❌")
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
    val isPreOrder: Boolean = false,
    val preOrderDate: String = "",
    val notes: String = ""
) {
    constructor() : this("", "", "", "", emptyList(), 0.0, OrderStatus.PENDING.name, System.currentTimeMillis(), System.currentTimeMillis(), null, false, "", "")
}
