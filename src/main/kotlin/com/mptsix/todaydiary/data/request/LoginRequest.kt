package com.mptsix.todaydiary.data.request

data class LoginRequest(
    var userId: String, // Email Address
    var userPassword: String
)