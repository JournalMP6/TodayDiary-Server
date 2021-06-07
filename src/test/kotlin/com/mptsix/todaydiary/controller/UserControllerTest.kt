package com.mptsix.todaydiary.controller

import com.mptsix.todaydiary.data.request.LoginRequest
import com.mptsix.todaydiary.data.request.PasswordChangeRequest
import com.mptsix.todaydiary.data.request.UserRegisterRequest
import com.mptsix.todaydiary.data.response.JournalResponse
import com.mptsix.todaydiary.data.response.LoginResponse
import com.mptsix.todaydiary.data.response.UserFiltered
import com.mptsix.todaydiary.data.response.UserRegisterResponse
import com.mptsix.todaydiary.data.user.User
import com.mptsix.todaydiary.data.user.journal.Journal
import com.mptsix.todaydiary.data.user.journal.JournalCategory
import com.mptsix.todaydiary.service.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.core.io.ByteArrayResource
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.remove
import org.springframework.http.*
import org.springframework.http.ResponseEntity.status
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.context.WebApplicationContext
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
internal class UserControllerTest {
    @LocalServerPort
    private var port: Int? = 8080

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private val restTemplate: TestRestTemplate = TestRestTemplate()

    private var serverUrl: String = "http://localhost:$port"
    private lateinit var mockMvc : MockMvc

    @BeforeEach
    @AfterEach
    fun clearDb() {
        serverUrl = "http://localhost:$port"
        mongoTemplate.remove<User>(Query())
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    fun loginUser(): String {
        val userRegisterRequest: UserRegisterRequest = UserRegisterRequest(
            userId = "KangDroid",
            userPassword = "test",
            userName = "KDR",
            userDateOfBirth = "WHENEVER",
            userPasswordAnswer = "WHAT",
            userPasswordQuestion = "WHAT"
        )
        userService.registerUser(userRegisterRequest)

        return userService.loginUser(
            LoginRequest(
                userId = userRegisterRequest.userId,
                userPassword = userRegisterRequest.userPassword
            )
        ).userToken
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

    @Test
    fun is_registerJournal_works_well() {
        val mockJournal: Journal = Journal(
            mainJournalContent = "Today was great!",
            journalLocation = "Somewhere over the rainbow!",
            journalCategory = JournalCategory.ACHIEVEMENT_JOURNAL,
            journalWeather = "Sunny",
            journalDate = System.currentTimeMillis()
        )
        val userToken: String = loginUser()
        val httpHeaders: HttpHeaders = HttpHeaders().apply {
            put("X-AUTH-TOKEN", listOf(userToken))
        }
        val journalResponse: ResponseEntity<JournalResponse> =
            restTemplate.exchange("${serverUrl}/api/v1/journal", HttpMethod.POST, HttpEntity(mockJournal, httpHeaders))

        assertThat(journalResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(journalResponse.hasBody()).isEqualTo(true)
        assertThat(journalResponse.body).isNotEqualTo(null)
        assertThat(journalResponse.body!!.registeredJournal.mainJournalContent)
            .isEqualTo(mockJournal.mainJournalContent)
    }

    @Test
    fun is_getJournal_works_well() {
        val mockJournal: Journal = Journal(
            mainJournalContent = "Today was great!",
            journalLocation = "Somewhere over the rainbow!",
            journalCategory = JournalCategory.ACHIEVEMENT_JOURNAL,
            journalWeather = "Sunny",
            journalDate = System.currentTimeMillis()
        )
        val loginToken: String = loginUser()
        val httpHeaders: HttpHeaders = HttpHeaders().apply {
            put("X-AUTH-TOKEN", listOf(loginToken))
        }
        userService.registerJournal(loginToken, mockJournal)
        val url: String = "${serverUrl}/api/v1/journal/${mockJournal.journalDate}"
        val responseEntity: ResponseEntity<Journal> =
            restTemplate.exchange(url, HttpMethod.GET, HttpEntity<Unit>(httpHeaders))

        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(responseEntity.hasBody()).isEqualTo(true)
        assertThat(responseEntity.body!!.journalDate).isEqualTo(mockJournal.journalDate)
    }

    @Test
    fun is_changePassword_works_well() {
        val loginToken: String = loginUser()
        val url: String = "${serverUrl}/api/v1/user"
        val httpHeaders: HttpHeaders = HttpHeaders().apply {
            put("X-AUTH-TOKEN", listOf(loginToken))
        }
        val responseEntity: ResponseEntity<Unit> =
            restTemplate.exchange(url, HttpMethod.PUT, HttpEntity<PasswordChangeRequest>(PasswordChangeRequest("test"), httpHeaders))

        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    }

    @Test
    fun is_removeUser_works_well() {
        val loginToken: String = loginUser()
        val url: String = "${serverUrl}/api/v1/user"
        val httpHeaders: HttpHeaders = HttpHeaders().apply {
            put("X-AUTH-TOKEN", listOf(loginToken))
        }

        val responseEntity: ResponseEntity<Unit> =
            restTemplate.exchange(url, HttpMethod.DELETE, HttpEntity<Unit>(httpHeaders))

        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    }

    @Test
    fun is_getSealedUser_works_well() {
        val loginToken: String = loginUser()
        val url: String = "${serverUrl}/api/v1/user/sealed"
        val httpHeaders: HttpHeaders = HttpHeaders().apply {
            put("X-AUTH-TOKEN", listOf(loginToken))
        }

        val responseEntity: ResponseEntity<Unit> =
            restTemplate.exchange(url, HttpMethod.GET, HttpEntity<Unit>(httpHeaders))

        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun is_followUser_works_well() {
        val loginToken: String = loginUser()
        val url: String = "${serverUrl}/api/v1/user/follow/kangdroid"
        val httpHeaders: HttpHeaders = HttpHeaders().apply {
            put("X-AUTH-TOKEN", listOf(loginToken))
        }

        val responseEntity: ResponseEntity<Unit> =
            restTemplate.exchange(url, HttpMethod.POST, HttpEntity<Unit>(httpHeaders))

        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    }

    @Test
    fun is_unfollowUser_works_well() {
        val loginToken: String = loginUser()
        val url: String = "${serverUrl}/api/v1/user/follow/kangdroid"
        val httpHeaders: HttpHeaders = HttpHeaders().apply {
            put("X-AUTH-TOKEN", listOf(loginToken))
        }

        val responseEntity: ResponseEntity<Unit> =
            restTemplate.exchange(url, HttpMethod.DELETE, HttpEntity<Unit>(httpHeaders))

        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    }

    @Test
    fun is_getFollowingUser_works_well() {
        val loginToken: String = loginUser()
        val url: String = "${serverUrl}/api/v1/user/follow"
        val httpHeaders: HttpHeaders = HttpHeaders().apply {
            put("X-AUTH-TOKEN", listOf(loginToken))
        }

        val responseEntity: ResponseEntity<List<UserFiltered>> =
            restTemplate.exchange(url, HttpMethod.GET, HttpEntity<Unit>(httpHeaders))

        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(responseEntity.hasBody()).isEqualTo(true)
        assertThat(responseEntity.body!!.size).isEqualTo(0)
    }

    @Test
    fun is_findUserByName_works_well() {
        val loginToken: String = loginUser()
        val url: String = "${serverUrl}/api/v1/user/test"
        val httpHeaders: HttpHeaders = HttpHeaders().apply {
            put("X-AUTH-TOKEN", listOf(loginToken))
        }

        val responseEntity: ResponseEntity<List<UserFiltered>> =
            restTemplate.exchange(url, HttpMethod.GET, HttpEntity<Unit>(httpHeaders))

        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(responseEntity.hasBody()).isEqualTo(true)
        assertThat(responseEntity.body!!.size).isEqualTo(0)
    }
}