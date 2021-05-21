package com.example.shopchain.model

data class Shops(
    val sid: String = "",
    val shopName: String = "",
    val mobile: String = "",
    val shopImageUrl: String = "",
    val shopType: String = "",
    val rating: String = "Nil",
    val description: String = "Welcome to our Shop!",
    val timing: String = "Not Updated yet!",
    val currentStatus: String = "Closed",
    val address: String = "",
    val lat: Double? = 0.0,
    val long: Double? = 0.0,
    val city: String = "",
    val country: String = ""
)
