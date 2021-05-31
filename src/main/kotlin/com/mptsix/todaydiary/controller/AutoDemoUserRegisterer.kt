package com.mptsix.todaydiary.controller

import com.mptsix.todaydiary.data.request.UserRegisterRequest
import com.mptsix.todaydiary.service.UserService
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class AutoDemoUserRegisterer(
    private val userService: UserService
) {
    /**
     * Register Demo user for testing purpose.
     */
    @PostConstruct
    fun registerDemoUser() {
        val mockUserRegisterRequest: UserRegisterRequest = UserRegisterRequest(
            userId = "kangdroid@outlook.com",
            userPassword = "asdfasdf!",
            userName = "KangDroid",
            userDateOfBirth = "2020",
            userPasswordQuestion = "N/A",
            userPasswordAnswer = "N/A"
        )

        userService.registerUser(mockUserRegisterRequest)
    }
}