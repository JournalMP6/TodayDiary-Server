package com.mptsix.todaydiary.data.request

data class UserRegisterRequest(
    var userId: String, // Email Address
    var userPassword: String,
    var userName: String,
    var userDateOfBirth: String,
    var userPasswordQuestion: String,
    var userPasswordAnswer: String
)
