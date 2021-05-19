package com.mptsix.todaydiary.service

import com.mptsix.todaydiary.data.User
import com.mptsix.todaydiary.data.UserRepository
import com.mptsix.todaydiary.data.request.UserRegisterRequest
import com.mptsix.todaydiary.data.response.UserRegisterResponse
import com.mptsix.todaydiary.error.exception.ConflictException
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    /**
     * Check whether user entity exists
     * returns true when entity exists, false when entity does not exists
     */
    private fun checkUserExistsByUserId(userId: String): Boolean {
        runCatching {
            userRepository.findByUserId(userId)
        }.onFailure {
            return false
        }
        return true
    }

    fun registerUser(userRegisterRequest: UserRegisterRequest): UserRegisterResponse {
        if (checkUserExistsByUserId(userRegisterRequest.userId)) {
            throw ConflictException("User ID: ${userRegisterRequest.userId} already exists!")
        }

        // Register
        val registeredUser: User = userRepository.addUser(userRegisterRequest.toUser())
        return UserRegisterResponse(
            registeredId = registeredUser.userId
        )
    }
}