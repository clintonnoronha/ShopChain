package com.example.shopchain.model

data class User(val uid: String = "",
                val username: String = "",
                val mobile: String = "",
                val profileImageUrl: String = "",
                val shopOwner: Boolean = false,
                val usid: String = ""
)
