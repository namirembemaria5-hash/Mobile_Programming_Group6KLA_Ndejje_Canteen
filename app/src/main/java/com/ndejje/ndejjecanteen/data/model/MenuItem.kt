package com.ndejje.ndejjecanteen.data.model

import com.google.firebase.firestore.PropertyName

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = MenuCategory.SNACKS.name,
    val imageUrl: String = "",
    
    @get:PropertyName("isAvailable")
    @set:PropertyName("isAvailable")
    var isAvailable: Boolean = true,
    
    @get:PropertyName("isWeekendOnly")
    @set:PropertyName("isWeekendOnly")
    var isWeekendOnly: Boolean = false,
    
    val subCategory: String = "",
    
    @get:PropertyName("isSauceOption")
    @set:PropertyName("isSauceOption")
    var isSauceOption: Boolean = false
) {
    constructor() : this("", "", "", 0.0, MenuCategory.SNACKS.name, "", true, false, "", false)
}

object DefaultMenuItems {
    val snacks = listOf(
        MenuItem("snack_1", "Mandaazi", "Fresh, warm Ugandan doughnuts", 500.0, MenuCategory.SNACKS.name),
        MenuItem("snack_2", "Samosas", "Crispy triangular pastry filled with spiced meat or veggies", 1000.0, MenuCategory.SNACKS.name),
        MenuItem("snack_3", "Cassava Slices", "Deep-fried golden cassava chips", 1000.0, MenuCategory.SNACKS.name),
        MenuItem("snack_4", "Kikomando", "Chapati and beans — a student favourite", 2000.0, MenuCategory.SNACKS.name)
    )

    val drinks = listOf(
        MenuItem("drink_1", "Red Bull", "Energy drink — 250ml", 5000.0, MenuCategory.DRINKS.name, subCategory = "energy"),
        MenuItem("drink_2", "Monster Energy", "Energy drink — 500ml", 6000.0, MenuCategory.DRINKS.name, subCategory = "energy"),
        MenuItem("drink_3", "Sting Energy", "Energy drink — 330ml", 3000.0, MenuCategory.DRINKS.name, subCategory = "energy"),
        MenuItem("drink_4", "Aqua Safe Water", "500ml mineral water", 1000.0, MenuCategory.DRINKS.name, subCategory = "water"),
        MenuItem("drink_5", "Rwenzori Water", "1.5L mineral water", 2000.0, MenuCategory.DRINKS.name, subCategory = "water"),
        MenuItem("drink_6", "Pepsi Cola", "300ml chilled soda", 2000.0, MenuCategory.DRINKS.name, subCategory = "soda"),
        MenuItem("drink_7", "Coca Cola", "300ml chilled soda", 2000.0, MenuCategory.DRINKS.name, subCategory = "soda"),
        MenuItem("drink_8", "Mirinda Orange", "300ml orange soda", 2000.0, MenuCategory.DRINKS.name, subCategory = "soda"),
        MenuItem("drink_9", "Sprite", "300ml lemon-lime soda", 2000.0, MenuCategory.DRINKS.name, subCategory = "soda"),
        MenuItem("drink_10", "Mango Juice", "Fresh mango juice — 300ml", 2500.0, MenuCategory.DRINKS.name, subCategory = "juice"),
        MenuItem("drink_11", "Passion Juice", "Fresh passion fruit juice — 300ml", 2500.0, MenuCategory.DRINKS.name, subCategory = "juice"),
        MenuItem("drink_12", "Pineapple Juice", "Fresh pineapple juice — 300ml", 2500.0, MenuCategory.DRINKS.name, subCategory = "juice")
    )

    val teaCoffee = listOf(
        MenuItem("tea_1", "African Tea", "Classic Ugandan milk tea with ginger", 1000.0, MenuCategory.TEA_COFFEE.name),
        MenuItem("tea_2", "Black Tea", "Plain black tea with sugar", 500.0, MenuCategory.TEA_COFFEE.name),
        MenuItem("tea_3", "Lemon Tea", "Black tea with fresh lemon", 1000.0, MenuCategory.TEA_COFFEE.name),
        MenuItem("tea_4", "Instant Coffee", "Nescafé with milk and sugar", 1500.0, MenuCategory.TEA_COFFEE.name),
        MenuItem("tea_5", "Black Coffee", "Strong plain black coffee", 1000.0, MenuCategory.TEA_COFFEE.name),
        MenuItem("tea_6", "Coffee with Ginger", "Spiced coffee — warming blend", 1500.0, MenuCategory.TEA_COFFEE.name)
    )

    val buffetMains = listOf(
        MenuItem("buffet_1", "Rice", "Steamed white rice", 2000.0, MenuCategory.BUFFET.name, subCategory = "main"),
        MenuItem("buffet_2", "Matooke", "Steamed green banana — Ugandan staple", 2000.0, MenuCategory.BUFFET.name, subCategory = "main"),
        MenuItem("buffet_3", "Posho", "Maize meal — ugali style", 1500.0, MenuCategory.BUFFET.name, subCategory = "main"),
        MenuItem("buffet_4", "Chapati", "Soft flatbread — 2 pieces", 1500.0, MenuCategory.BUFFET.name, subCategory = "main"),
        MenuItem("buffet_5", "Irish Potatoes", "Boiled potatoes", 1500.0, MenuCategory.BUFFET.name, subCategory = "main"),
        MenuItem("buffet_6", "Pasta", "Boiled spaghetti / pasta", 2000.0, MenuCategory.BUFFET.name, subCategory = "main")
    )

    val buffetSauces = listOf(
        MenuItem("sauce_1", "Beans", "Stewed red beans", 1500.0, MenuCategory.BUFFET.name, subCategory = "sauce", isSauceOption = true),
        MenuItem("sauce_2", "Beef Stew", "Tender beef in rich sauce", 3000.0, MenuCategory.BUFFET.name, subCategory = "sauce", isSauceOption = true),
        MenuItem("sauce_3", "Groundnut Sauce", "Peanut-based sauce with vegetables", 2000.0, MenuCategory.BUFFET.name, subCategory = "sauce", isSauceOption = true),
        MenuItem("sauce_4", "Chicken Stew", "Slow-cooked chicken pieces", 4000.0, MenuCategory.BUFFET.name, subCategory = "sauce", isSauceOption = true),
        MenuItem("sauce_5", "Cabbage & Carrot", "Lightly cooked vegetable mix", 1000.0, MenuCategory.BUFFET.name, subCategory = "sauce", isSauceOption = true),
        MenuItem("sauce_6", "Fried Egg", "Two pan-fried eggs", 1500.0, MenuCategory.BUFFET.name, subCategory = "sauce", isSauceOption = true)
    )

    val specialOrders = listOf(
        MenuItem(
            "special_1",
            "Lusaniya",
            "Traditional roasted meat — available weekends only! Pre-order by Friday.",
            8000.0,
            MenuCategory.SPECIAL_ORDERS.name,
            isWeekendOnly = true
        )
    )

    fun allItems(): List<MenuItem> =
        snacks + drinks + teaCoffee + buffetMains + buffetSauces + specialOrders
}
