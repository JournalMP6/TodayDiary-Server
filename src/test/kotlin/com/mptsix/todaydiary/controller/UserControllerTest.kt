package com.mptsix.todaydiary.controller

import com.mptsix.todaydiary.data.request.LoginRequest
import com.mptsix.todaydiary.data.request.UserRegisterRequest
import com.mptsix.todaydiary.data.response.JournalResponse
import com.mptsix.todaydiary.data.response.LoginResponse
import com.mptsix.todaydiary.data.response.UserRegisterResponse
import com.mptsix.todaydiary.data.user.User
import com.mptsix.todaydiary.data.user.journal.Journal
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
            journalCategory = "Somewhat_category",
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
    fun is_registerJournalPicture_works_well() {
        val mockJournal: Journal = Journal(
            mainJournalContent = "Today was great!",
            journalLocation = "Somewhere over the rainbow!",
            journalCategory = "Somewhat_category",
            journalWeather = "Sunny",
            journalDate = 2000
        )
        val userToken: String = loginUser()

        userService.registerJournal(userToken, mockJournal)

        // make uploadFile
        val uploadFileName: String = "uploadTest-service.txt"
        val uploadFileContent: ByteArray = "file upload test file!".toByteArray()
        val multipartFile: MockMultipartFile = MockMultipartFile(
            "uploadFile", uploadFileName, "text/plain", uploadFileContent
        )

        // Perform
        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/api/v1/journal/picture")
                .file(multipartFile)
                .headers(HttpHeaders().apply {
                    add("X-AUTH-TOKEN", userToken)
                    add("JOURNAL-DATE", "${mockJournal.journalDate}")
                })
        ).andExpect { status(HttpStatus.NO_CONTENT) }
            .andDo(MockMvcResultHandlers.print())
            .andDo{
                assertThat(it.response.status).isEqualTo(HttpStatus.NO_CONTENT.value())
            }

    }
}