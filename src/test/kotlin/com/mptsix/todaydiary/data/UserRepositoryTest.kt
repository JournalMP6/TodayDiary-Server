package com.mptsix.todaydiary.data

import com.mptsix.todaydiary.data.user.User
import com.mptsix.todaydiary.data.user.UserRepository
import com.mptsix.todaydiary.data.user.journal.Journal
import com.mptsix.todaydiary.data.user.journal.JournalCategory
import com.mptsix.todaydiary.error.exception.NotFoundException
import com.mptsix.todaydiary.error.exception.UnknownErrorException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.remove
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
internal class UserRepositoryTest {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    private val mockUser: User = User(
        userId = "KangDroid",
        userPassword = "test",
        userName = "KDR",
        userDateOfBirth = "WHENEVER",
        userPasswordAnswer = "WHAT",
        userPasswordQuestion = "WHAT"
    )

    @BeforeEach
    @AfterEach
    private fun initDataRepository() {
        mongoTemplate.remove<User>(Query())
    }

    @Test
    fun is_addUser_works_well() {
        assertThat(userRepository.addUser(mockUser).userId).isEqualTo(mockUser.userId)
    }

    @Test
    fun is_findByUserId_throws_npe() {
        runCatching {
            userRepository.findByUserId("test")
        }.onSuccess {
            fail("We did not initiated user db but it succeed!")
        }.onFailure {
            assertThat(it is NullPointerException).isEqualTo(true)
        }
    }

    @Test
    fun is_findByUserId_works_well() {
        mongoTemplate.save(mockUser)

        runCatching {
            userRepository.findByUserId(mockUser.userId)
        }.onFailure {
            println(it.stackTraceToString())
            fail(it.message)
        }.onSuccess {
            assertThat(it.userId).isEqualTo(mockUser.userId)
            assertThat(it.userPassword).isEqualTo(mockUser.userPassword)
            assertThat(it.userName).isEqualTo(mockUser.userName)
            assertThat(it.userDateOfBirth).isEqualTo(mockUser.userDateOfBirth)
            assertThat(it.userPasswordAnswer).isEqualTo(mockUser.userPasswordAnswer)
            assertThat(it.userPasswordQuestion).isEqualTo(mockUser.userPasswordQuestion)
        }
    }

    @Test
    fun is_findByUserName_throws_npe() {
        runCatching {
            userRepository.findByUserName("test")
        }.onSuccess {
            fail("We did not initiated user db but it succeed!")
        }.onFailure {
            assertThat(it is NullPointerException).isEqualTo(true)
        }
    }

    @Test
    fun is_findByUserName_works_well() {
        mongoTemplate.save(mockUser)

        runCatching {
            userRepository.findByUserName(mockUser.userName)
        }.onFailure {
            println(it.stackTraceToString())
            fail(it.message)
        }.onSuccess {
            assertThat(it.userId).isEqualTo(mockUser.userId)
            assertThat(it.userPassword).isEqualTo(mockUser.userPassword)
            assertThat(it.userName).isEqualTo(mockUser.userName)
            assertThat(it.userDateOfBirth).isEqualTo(mockUser.userDateOfBirth)
            assertThat(it.userPasswordAnswer).isEqualTo(mockUser.userPasswordAnswer)
            assertThat(it.userPasswordQuestion).isEqualTo(mockUser.userPasswordQuestion)
        }
    }

    @Test
    fun is_findCategorySizeByUserId_works_well() {
        val mockUserWithJournal: User = User(
            userId = "KangDroid",
            userPassword = "test",
            userName = "KDR",
            userDateOfBirth = "WHENEVER",
            userPasswordAnswer = "WHAT",
            userPasswordQuestion = "WHAT",
            journalData = mutableListOf(
                Journal(
                    mainJournalContent = "",
                    journalLocation = "",
                    journalCategory = JournalCategory.ACHIEVEMENT_JOURNAL,
                    journalWeather = "",
                    journalDate = -1
                ),
                Journal(
                    mainJournalContent = "",
                    journalLocation = "",
                    journalCategory = JournalCategory.ACHIEVEMENT_JOURNAL,
                    journalWeather = "",
                    journalDate = -1
                ),
                Journal(
                    mainJournalContent = "",
                    journalLocation = "",
                    journalCategory = JournalCategory.ACHIEVEMENT_JOURNAL,
                    journalWeather = "",
                    journalDate = -1
                )
            )
        )
        userRepository.addUser(mockUserWithJournal)

        runCatching {
            userRepository.findCategorySizeByUserId(JournalCategory.ACHIEVEMENT_JOURNAL.name, "KangDroid")
        }.onFailure {
            println(it.stackTraceToString())
            fail("It failed!")
        }.onSuccess {
            assertThat(it).isEqualTo(3)
        }
    }

    @Test
    fun is_findCategorySizeByUserId_returns_zero() {
        val mockUserWithJournal: User = User(
            userId = "KangDroid",
            userPassword = "test",
            userName = "KDR",
            userDateOfBirth = "WHENEVER",
            userPasswordAnswer = "WHAT",
            userPasswordQuestion = "WHAT",
            journalData = mutableListOf()
        )
        userRepository.addUser(mockUserWithJournal)

        runCatching {
            userRepository.findCategorySizeByUserId("TEST", "KangDroid")
        }.onSuccess {
            assertThat(it).isEqualTo(0)
        }
    }

    @Test
    fun is_removeUser_works_well() {
        val mockUserWithJournal: User = User(
            userId = "KangDroid",
            userPassword = "test",
            userName = "KDR",
            userDateOfBirth = "WHENEVER",
            userPasswordAnswer = "WHAT",
            userPasswordQuestion = "WHAT",
            journalData = mutableListOf()
        )
        userRepository.addUser(mockUserWithJournal)

        runCatching {
            userRepository.removeUser(mockUserWithJournal.userId)
        }.onFailure {
            println(it.stackTraceToString())
            fail("Should be succeed!")
        }.onSuccess {
            val userList: List<User> = mongoTemplate.findAll()
            assertThat(userList.isEmpty()).isEqualTo(true)
        }
    }

    @Test
    fun is_removeUser_throws_unknownerror() {
        runCatching {
            userRepository.removeUser("mockUserWithJournal.userId")
        }.onFailure {
            assertThat(it is UnknownErrorException).isEqualTo(true)
        }.onSuccess {
            fail("User list is empty but it succeed?")
        }
    }

}