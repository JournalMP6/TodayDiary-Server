package com.mptsix.todaydiary.controller

import com.mptsix.todaydiary.data.request.UserRegisterRequest
import com.mptsix.todaydiary.data.response.UserRegisterResponse
import com.mptsix.todaydiary.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService
) {
    @PostMapping("/api/v1/user")
    fun registerUser(@RequestBody userRegisterRequest: UserRegisterRequest): ResponseEntity<UserRegisterResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(userService.registerUser(userRegisterRequest))
    }
}