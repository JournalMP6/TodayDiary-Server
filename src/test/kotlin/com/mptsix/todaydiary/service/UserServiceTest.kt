package com.mptsix.todaydiary.service

import com.mptsix.todaydiary.data.user.User
import com.mptsix.todaydiary.data.request.LoginRequest
import com.mptsix.todaydiary.data.request.PasswordChangeRequest
import com.mptsix.todaydiary.data.request.UserRegisterRequest
import com.mptsix.todaydiary.data.user.UserRepository
import com.mptsix.todaydiary.data.user.journal.Journal
import com.mptsix.todaydiary.data.user.journal.JournalCategory
import com.mptsix.todaydiary.data.user.journal.JournalImage
import com.mptsix.todaydiary.error.exception.ConflictException
import com.mptsix.todaydiary.error.exception.ForbiddenException
import com.mptsix.todaydiary.error.exception.NotFoundException
import com.mptsix.todaydiary.security.JWTTokenProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.remove
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.lang.reflect.Method

@SpringBootTest
@ExtendWith(SpringExtension::class)
internal class UserServiceTest {
    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var jwtTokenProvider: JWTTokenProvider

    private val mockUser: User = User(
        userId = "KangDroid",
        userPassword = "test",
        userName = "KDR",
        userDateOfBirth = "WHENEVER",
        userPasswordAnswer = "WHAT",
        userPasswordQuestion = "WHAT"
    )

    fun loginUser(): String {
        userService.registerUser(
            UserRegisterRequest(
                userId = mockUser.userId,
                userPassword =  mockUser.userPassword,
                userName = mockUser.userName,
                userDateOfBirth = mockUser.userDateOfBirth,
                userPasswordAnswer = mockUser.userPasswordAnswer,
                userPasswordQuestion = mockUser.userPasswordQuestion
            )
        )

        return userService.loginUser(
            LoginRequest(
                userId = mockUser.userId,
                userPassword = mockUser.userPassword
            )
        ).userToken
    }

    @BeforeEach
    @AfterEach
    fun initDb() {
        mongoTemplate.remove<User>(Query())
    }

    @Test
    fun is_checkUserExistsByUserId_returns_true() {
        // Save DB First
        mongoTemplate.save(mockUser)

        // Get Function
        val method: Method = UserService::class.java.getDeclaredMethod("checkUserExistsByUserId", String::class.java).apply {
            isAccessible = true
        }
        val retVal: Boolean = method.invoke(userService, mockUser.userId) as Boolean

        assertThat(retVal).isEqualTo(true)
    }

    @Test
    fun is_checkUserExistsByUserId_returns_false() {
        // Get Function
        val method: Method = UserService::class.java.getDeclaredMethod("checkUserExistsByUserId", String::class.java).apply {
            isAccessible = true
        }
        val retVal: Boolean = method.invoke(userService, mockUser.userId) as Boolean

        assertThat(retVal).isEqualTo(false)
    }

    @Test
    fun is_registerUser_normal_works_well() {
        val userRegisterRequest: UserRegisterRequest = UserRegisterRequest(
            userId = "KangDroid",
            userPassword = "test",
            userName = "KDR",
            userDateOfBirth = "WHENEVER",
            userPasswordAnswer = "WHAT",
            userPasswordQuestion = "WHAT"
        )

        assertThat(userService.registerUser(userRegisterRequest).registeredId).isEqualTo(userRegisterRequest.userId)
    }

    @Test
    fun is_registerUser_duplicated_fails() {
        val userRegisterRequest: UserRegisterRequest = UserRegisterRequest(
            userId = "KangDroid",
            userPassword = "test",
            userName = "KDR",
            userDateOfBirth = "WHENEVER",
            userPasswordAnswer = "WHAT",
            userPasswordQuestion = "WHAT"
        )
        userService.registerUser(userRegisterRequest)

        // Save Again[Duplicated]
        runCatching {
            userService.registerUser(userRegisterRequest)
        }.onSuccess {
            fail("Duplicated Registration but succeed?")
        }.onFailure {
            assertThat(it is ConflictException).isEqualTo(true)
        }

    }

    @Test
    fun is_loginUser_works_well() {
        // Register
        val userRegisterRequest: UserRegisterRequest = UserRegisterRequest(
            userId = "KangDroid",
            userPassword = "test",
            userName = "KDR",
            userDateOfBirth = "WHENEVER",
            userPasswordAnswer = "WHAT",
            userPasswordQuestion = "WHAT"
        )
        userService.registerUser(userRegisterRequest)

        val loginRequest: LoginRequest = LoginRequest(
            userId = userRegisterRequest.userId,
            userPassword = userRegisterRequest.userPassword
        )

        runCatching {
            userService.loginUser(loginRequest)
        }.onFailure {
            fail("We registered user, but login failed?")
        }.onSuccess {
            assertThat(it.userToken).isNotEqualTo("")
            assertThat(jwtTokenProvider.getUserPk(it.userToken)).isEqualTo(userRegisterRequest.userId)
        }
    }

    @Test
    fun is_loginUser_wrong_id() {
        val loginRequest: LoginRequest = LoginRequest(
            userId = "userRegisterRequest.userId",
            userPassword = "userRegisterRequest.userPassword"
        )

        runCatching {
            userService.loginUser(loginRequest)
        }.onFailure {
            assertThat(it is NotFoundException).isEqualTo(true)
        }.onSuccess {
            fail("DB is empty but login succeed somehow")
        }
    }

    @Test
    fun is_loginUser_wrong_password() {
        // Register
        val userRegisterRequest: UserRegisterRequest = UserRegisterRequest(
            userId = "KangDroid",
            userPassword = "test",
            userName = "KDR",
            userDateOfBirth = "WHENEVER",
            userPasswordAnswer = "WHAT",
            userPasswordQuestion = "WHAT"
        )
        userService.registerUser(userRegisterRequest)

        val loginRequest: LoginRequest = LoginRequest(
            userId = userRegisterRequest.userId,
            userPassword = "userRegisterRequest.userPassword"
        )

        runCatching {
            userService.loginUser(loginRequest)
        }.onFailure {
            assertThat(it is ForbiddenException).isEqualTo(true)
        }.onSuccess {
            fail("DB is empty but login succeed somehow")
        }
    }

    @Test
    fun is_registerJournal_404_crazy_token() {
        runCatching {
            userService.registerJournal("whatever", Journal("", "", JournalCategory.ACHIEVEMENT_JOURNAL, "", 10, JournalImage()))
        }.onSuccess {
            fail("Token is invalid and it succeed?")
        }.onFailure {
            assertThat(it is NotFoundException).isEqualTo(true)
        }
    }

    @Test
    fun is_registerJournal_registers_well() {
        val userToken: String = loginUser()
        val mockJournal: Journal = Journal(
            mainJournalContent = "Today was great!",
            journalLocation = "Somewhere over the rainbow!",
            journalCategory = JournalCategory.ACHIEVEMENT_JOURNAL,
            journalWeather = "Sunny",
            journalDate = System.currentTimeMillis()
        )

        runCatching {
            userService.registerJournal(userToken, mockJournal)
        }.onFailure {
            fail("All data was set but it failed. StackTrace: ${it.stackTraceToString()}")
        }.onSuccess {
            assertThat(it.registeredJournal.mainJournalContent).isEqualTo(mockJournal.mainJournalContent)
        }
    }

    @Test
    fun is_registerJournal_edit_well() {
        val userToken: String = loginUser()
        val mockJournal: Journal = Journal(
            mainJournalContent = "Today was great!",
            journalLocation = "Somewhere over the rainbow!",
            journalCategory = JournalCategory.ACHIEVEMENT_JOURNAL,
            journalWeather = "Sunny",
            journalDate = System.currentTimeMillis()
        )
        userService.registerJournal(userToken, mockJournal)

        runCatching {
            userService.registerJournal(userToken, mockJournal.apply {mainJournalContent = "test"})
        }.onFailure {
            fail("All data was set but it failed. StackTrace: ${it.stackTraceToString()}")
        }.onSuccess {
            assertThat(it.registeredJournal.mainJournalContent).isEqualTo("test")
            assertThat(it.registeredJournal.journalLocation).isEqualTo(mockJournal.journalLocation)
        }
    }

    @Test
    fun is_getJournal_throws_404() {
        val loginToken: String = loginUser()
        runCatching {
            userService.getJournal(loginToken, -1000)
        }.onSuccess {
            fail("We do not have any journal but it succeed?")
        }.onFailure {
            assertThat(it is NotFoundException).isEqualTo(true)
        }
    }

    @Test
    fun is_getJournal_works_well() {
        val loginToken: String = loginUser()
        val mockJournal: Journal = Journal(
            mainJournalContent = "Today was great!",
            journalLocation = "Somewhere over the rainbow!",
            journalCategory = JournalCategory.ACHIEVEMENT_JOURNAL,
            journalWeather = "Sunny",
            journalDate = 2000
        )
        userService.registerJournal(loginToken, mockJournal)

        runCatching {
            userService.getJournal(loginToken, mockJournal.journalDate)
        }.onSuccess {
            assertThat(it.journalDate).isEqualTo(mockJournal.journalDate)
            assertThat(it.mainJournalContent).isEqualTo(mockJournal.mainJournalContent)
            assertThat(it.journalLocation).isEqualTo(mockJournal.journalLocation)
            assertThat(it.journalCategory).isEqualTo(mockJournal.journalCategory)
            assertThat(it.journalWeather).isEqualTo(mockJournal.journalWeather)
        }.onFailure {
            println(it.stackTraceToString())
            fail("We've mocked up journal data but it failed")
        }
    }

    @Test
    fun is_changePassword_works_well() {
        val loginToken: String = loginUser()
        userService.changePassword(loginToken, PasswordChangeRequest("whatever"))

        val user: User = userRepository.findByUserId(mockUser.userId)
        assertThat(user.userPassword).isEqualTo("whatever")
    }

    @Test
    fun is_removeUser_works_well() {
        val loginToken: String = loginUser()
        userService.removeUser(loginToken)

        val userList: List<User> = mongoTemplate.findAll()
        assertThat(userList.isEmpty()).isEqualTo(true)
    }
}