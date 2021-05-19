package com.mptsix.todaydiary.data.request

import com.mptsix.todaydiary.data.User

data class UserRegisterRequest(
    var userId: String, // Email Address
    var userPassword: String,
    var userName: String,
    var userDateOfBirth: String,
    var userPasswordQuestion: String,
    var userPasswordAnswer: String
) {
    fun toUser(): User {
        return User(
            userId = userId,
            userPassword = userPassword,
            userName = userName,
            userDateOfBirth = userDateOfBirth,
            userPasswordAnswer = userPasswordAnswer,
            userPasswordQuestion = userPasswordQuestion,
            roles = setOf("ROLE_USER")
        )
    }
}
