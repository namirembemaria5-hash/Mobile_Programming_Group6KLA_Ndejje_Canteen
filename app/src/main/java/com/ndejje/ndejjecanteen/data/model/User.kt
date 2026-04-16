package com.ndejje.ndejjecanteen.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val fcmToken: String = "",
    val role: String = "USER" // Roles: USER, ADMIN, DELIVERY, KITCHEN
) {
    constructor() : this("", "", "", "", "", "USER")
}
