package com.mptsix.todaydiary.controller

import com.mptsix.todaydiary.data.user.User
import com.mptsix.todaydiary.data.request.LoginRequest
import com.mptsix.todaydiary.data.request.UserRegisterRequest
import com.mptsix.todaydiary.data.response.LoginResponse
import com.mptsix.todaydiary.data.response.UserRegisterResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.remove
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
internal class UserControllerTest {
    @LocalServerPort
    private var port: Int? = 8080

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    private val restTemplate: TestRestTemplate = TestRestTemplate()

    private var serverUrl: String = "http://localhost:$port"

    @BeforeEach
    @AfterEach
    fun clearDb() {
        serverUrl = "http://localhost:$port"
        mongoTemplate.remove<User>(Query())
    }

    @Test
    fun is_registerUser_works_well() {
        val userRegisterRequest: UserRegisterRequest = UserRegisterRequest(
            userId = "KangDroid",
            userPassword = "test",
            userName = "KDR",
            userDateOfBirth = "WHENEVER",
            userPasswordAnswer = "WHAT",
            userPasswordQuestion = "WHAT"
        )
        val responseEntity: ResponseEntity<UserRegisterResponse> =
            restTemplate.postForEntity("${serverUrl}/api/v1/user", userRegisterRequest)

        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(responseEntity.hasBody()).isEqualTo(true)
        assertThat(responseEntity.body!!.registeredId).isEqualTo(userRegisterRequest.userId)
    }

    @Test
    fun is_loginUser_works_well() {
        val userRegisterRequest: UserRegisterRequest = UserRegisterRequest(
            userId = "KangDroid",
            userPassword = "test",
            userName = "KDR",
            userDateOfBirth = "WHENEVER",
            userPasswordAnswer = "WHAT",
            userPasswordQuestion = "WHAT"
        )
        restTemplate.postForEntity<UserRegisterResponse>("${serverUrl}/api/v1/user", userRegisterRequest)

        val loginRequest: LoginRequest = LoginRequest(
            userId = userRegisterRequest.userId,
            userPassword = userRegisterRequest.userPassword
        )

        val responseEntity: ResponseEntity<LoginResponse> =
            restTemplate.postForEntity("${serverUrl}/api/v1/login", loginRequest)

        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(responseEntity.hasBody()).isEqualTo(true)
        assertThat(responseEntity.body!!.userToken).isNotEqualTo("")
    }

}