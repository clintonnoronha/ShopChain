package com.example.shopchain.model

data class Products(
        val pid: Int = 0,
        val pImageUrl: String = "",
        val pName: String = "",
        val pPrice: String = "",
        val pStatus: Boolean = false
)
