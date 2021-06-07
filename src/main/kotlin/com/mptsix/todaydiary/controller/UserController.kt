package com.mptsix.todaydiary.controller

import com.mptsix.todaydiary.data.request.LoginRequest
import com.mptsix.todaydiary.data.request.PasswordChangeRequest
import com.mptsix.todaydiary.data.request.UserRegisterRequest
import com.mptsix.todaydiary.data.response.*
import com.mptsix.todaydiary.data.user.journal.Journal
import com.mptsix.todaydiary.service.UserService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

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

    @PostMapping("/api/v1/login")
    fun loginUser(@RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(userService.loginUser(loginRequest))
    }

    @PostMapping("/api/v1/journal")
    fun registerJournal(@RequestHeader header: HttpHeaders, @RequestBody journal: Journal): ResponseEntity<JournalResponse> {
        val userToken: String = header["X-AUTH-TOKEN"]!![0]
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(userService.registerJournal(userToken, journal))
    }

    @GetMapping("/api/v1/journal/{time}")
    fun getJournal(@RequestHeader header: HttpHeaders, @PathVariable("time") journalDate: Long): ResponseEntity<Journal> {
        val userToken: String = header["X-AUTH-TOKEN"]!![0]
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                userService.getJournal(userToken, journalDate)
            )
    }

    @PutMapping("/api/v1/user")
    fun changePassword(@RequestHeader header: HttpHeaders, @RequestBody passwordChangeRequest: PasswordChangeRequest): ResponseEntity<Unit> {
        val userToken: String = header["X-AUTH-TOKEN"]!![0]
        userService.changePassword(userToken, passwordChangeRequest)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/api/v1/user")
    fun removeUser(@RequestHeader header: HttpHeaders): ResponseEntity<Unit> {
        val userToken: String = header["X-AUTH-TOKEN"]!![0]
        userService.removeUser(userToken)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/api/v1/user/sealed")
    fun getSealedUser(@RequestHeader header: HttpHeaders): ResponseEntity<UserSealed> {
        val userToken: String = header["X-AUTH-TOKEN"]!![0]
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(userService.getUserSealed(userToken))
    }

    @PostMapping("/api/v1/user/follow/{id}")
    fun followUser(@RequestHeader header: HttpHeaders, @PathVariable("id") userId: String): ResponseEntity<Unit> {
        val userToken: String = header["X-AUTH-TOKEN"]!![0]
        userService.followUser(userToken, userId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/api/v1/user/follow/{id}")
    fun unfollowUser(@RequestHeader header: HttpHeaders, @PathVariable("id") userId: String): ResponseEntity<Unit> {
        val userToken: String = header["X-AUTH-TOKEN"]!![0]
        userService.unfollowUser(userToken, userId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/api/v1/user/follow")
    fun getFollowingUser(@RequestHeader header: HttpHeaders): ResponseEntity<List<UserFiltered>> {
        val userToken: String = header["X-AUTH-TOKEN"]!![0]

        return ResponseEntity.ok(
            userService.getFollowingUser(userToken)
        )
    }

    @GetMapping("/api/v1/user/{name}")
    fun findUserByName(@RequestHeader header: HttpHeaders, @PathVariable("name") targetName: String): ResponseEntity<List<UserFiltered>> {
        val userToken: String = header["X-AUTH-TOKEN"]!![0]
        return ResponseEntity.ok(
            userService.findUserByName(userToken, targetName)
        )
    }
}