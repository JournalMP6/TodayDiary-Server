package com.mptsix.todaydiary.service

import com.mptsix.todaydiary.data.UserRepository
import com.mptsix.todaydiary.data.request.UserRegisterRequest
import com.mptsix.todaydiary.data.response.UserRegisterResponse
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun registerUser(userRegisterRequest: UserRegisterRequest): UserRegisterResponse {

    }
}