package com.mptsix.todaydiary.service

import com.mptsix.todaydiary.data.user.User
import com.mptsix.todaydiary.data.user.UserRepository
import com.mptsix.todaydiary.data.request.LoginRequest
import com.mptsix.todaydiary.data.request.PasswordChangeRequest
import com.mptsix.todaydiary.data.request.UserRegisterRequest
import com.mptsix.todaydiary.data.response.*
import com.mptsix.todaydiary.data.user.journal.Journal
import com.mptsix.todaydiary.data.user.journal.JournalCategory
import com.mptsix.todaydiary.data.user.journal.JournalImage
import com.mptsix.todaydiary.error.exception.ConflictException
import com.mptsix.todaydiary.error.exception.ForbiddenException
import com.mptsix.todaydiary.error.exception.NotFoundException
import com.mptsix.todaydiary.security.JWTTokenProvider
import org.bson.BsonBinarySubType
import org.bson.types.Binary
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import kotlin.streams.toList

@Service
class UserService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JWTTokenProvider
) {
    // Logger
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

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

    /**
     * Check whether user entity exists with userToken
     */
    private fun getUserIdFromToken(userToken: String): String {
        return runCatching {
            jwtTokenProvider.getUserPk(userToken)
        }.getOrElse {
            logger.error("Cannot find userID from user token! Probably expired or malformed?")
            logger.error("StackTrace: ${it.stackTraceToString()}")
            throw NotFoundException("Cannot find userID from user token!")
        }
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

    fun loginUser(loginRequest: LoginRequest): LoginResponse {
        if (!checkUserExistsByUserId(loginRequest.userId)) {
            logger.error("Cannot find username: ${loginRequest.userId} throwing 404")
            throw NotFoundException("Cannot find userid: ${loginRequest.userId}")
        }
        val user: User = userRepository.findByUserId(loginRequest.userId)
        if (user.userPassword != loginRequest.userPassword) {
            logger.error("Username is correct, but user password is not found!")
            throw ForbiddenException("Username is correct, but user password is not correct!")
        }

        return LoginResponse(
            jwtTokenProvider.createToken(loginRequest.userId, user.roles.toList())
        )
    }

    // Journal
    fun registerJournal(userToken: String, journal: Journal): JournalResponse {
        val user: User = userRepository.findByUserId(getUserIdFromToken(userToken))
        val isEdit: Journal? = user.journalData.find {it.journalDate == journal.journalDate}
        if (isEdit == null) {
            // Not Found, Register
            logger.info("Journal for user: ${user.userId} && ${journal.journalDate} not found.")
            logger.info("Registering new journal data ${journal.journalDate}")
        } else {
            // Found, Edit
            logger.info("Journal is already exists! Just updating journal data..")
            user.journalData.removeIf {it.journalDate == journal.journalDate}
        }

        // Update User DB
        user.journalData.add(journal)
        userRepository.addUser(user)

        logger.info("Register journal!")
        logger.info(journal.journalImage.imageFile?.toString())

        return JournalResponse(journal)
    }

    fun getJournal(userToken: String, journalDate: Long): Journal {
        // get User Entity
        val user: User = userRepository.findByUserId(getUserIdFromToken(userToken))
        return user.journalData.find { it.journalDate == journalDate } ?: run {
            logger.error("Cannot get journal data for user: ${user.userId}, with journalDate: $journalDate")
            throw NotFoundException("Cannot get journal data for user: ${user.userId}, with journalDate: $journalDate")
        }
    }

    fun changePassword(userToken: String, passwordChangeRequest: PasswordChangeRequest) {
        val user: User = userRepository.findByUserId(getUserIdFromToken(userToken)).apply {
            userPassword = passwordChangeRequest.userPassword
        }
        userRepository.addUser(user)
    }

    fun removeUser(userToken: String) {
        userRepository.removeUser(getUserIdFromToken(userToken))
    }

    fun getUserSealed(userToken: String): UserSealed {
        val user: User = userRepository.findByUserId(getUserIdFromToken(userToken))

        return UserSealed(
            userId = user.userId,
            userName = user.userName,
            journalCategoryList = createJournalCategoryList(user.userId),
            journalList = createJournalSealedList(user)
        )
    }

    fun followUser(userToken: String, targetUserId: String) {
        val user: User = userRepository.findByUserId(getUserIdFromToken(userToken)).apply {
            followList.add(targetUserId)
        }

        userRepository.addUser(user)
    }

    fun unfollowUser(userToken: String, targetUserId: String) {
        val user: User = userRepository.findByUserId(getUserIdFromToken(userToken)).apply {
            followList.removeIf { it == targetUserId }
        }

        userRepository.addUser(user)
    }

    fun getFollowingUser(userToken: String): List<UserFiltered> {
        return userRepository.findByUserId(
            getUserIdFromToken(userToken)
        ).followList.map {
            val user: User = userRepository.findByUserId(it)
            UserFiltered(
                userName = user.userName,
                userId = user.userId,
                isUserFollowedTargetUser = true // Since we are getting following user.
            )
        }
    }

    fun findUserByName(userToken: String, userName: String): List<UserFiltered> {
        val user: User = userRepository.findByUserId(getUserIdFromToken(userToken))
        val userList: List<User> = userRepository.findAllByUserName(userName)

        return userList.map {
            UserFiltered(
                userName = it.userName,
                userId = it.userId,
                isUserFollowedTargetUser = (
                    user.followList.find { eachString ->
                        eachString == it.userId
                    }
                ) != null
            )
        }
    }

    private fun createJournalCategoryList(userId: String): List<JournalCategoryResponse> {
        return enumValues<JournalCategory>().map {
            JournalCategoryResponse(
                category = it,
                count = userRepository.findCategorySizeByUserId(it.name, userId)
            )
        }
    }

    private fun createJournalSealedList(user: User): List<JournalSealed> {
        return user.journalData.map {
            JournalSealed(
                timestamp = it.journalDate,
                mainContent = it.mainJournalContent
            )
        }.stream().limit(20).toList()
    }
}