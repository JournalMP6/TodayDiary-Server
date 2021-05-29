package com.mptsix.todaydiary.service

import com.mptsix.todaydiary.data.user.User
import com.mptsix.todaydiary.data.user.UserRepository
import com.mptsix.todaydiary.data.request.LoginRequest
import com.mptsix.todaydiary.data.request.UserRegisterRequest
import com.mptsix.todaydiary.data.response.JournalResponse
import com.mptsix.todaydiary.data.response.LoginResponse
import com.mptsix.todaydiary.data.response.UserRegisterResponse
import com.mptsix.todaydiary.data.user.journal.Journal
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

    private fun checkJournalExists(user: User, journalDate: Long): Journal {
        return user.journalData.find {
            it.journalDate == journalDate
        } ?: run {
            logger.error("Cannot find journal data from ${user.userId}, target: $journalDate!")
            throw NotFoundException("Cannot find Journal!")
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
        val user: User = userRepository.findByUserId(getUserIdFromToken(userToken)).apply {
            journalData.add(journal)
        }
        // Update User DB
        userRepository.addUser(user)

        return JournalResponse(journal)
    }

    fun registerJournalPicture(userToken: String, uploadPicture: MultipartFile, journalDate: Long) {
        // Get User Entity
        val user: User = userRepository.findByUserId(getUserIdFromToken(userToken))

        // Get Journal Data
        val journalData: Journal = checkJournalExists(user, journalDate).apply {
            journalImage = JournalImage(
                Binary(BsonBinarySubType.BINARY, uploadPicture.bytes)
            )
        }

        // Remove Journal Data from User
        user.journalData.removeIf { it.journalDate == journalDate }

        // Add Imaged-Data
        user.journalData.add(journalData)
        userRepository.addUser(user)
    }

    fun getJournal(userToken: String, journalDate: Long): Journal {
        // get User Entity
        val user: User = userRepository.findByUserId(getUserIdFromToken(userToken))
        return user.journalData.find { it.journalDate == journalDate } ?: run {
            logger.error("Cannot get journal data for user: ${user.userId}, with journalDate: $journalDate")
            throw NotFoundException("Cannot get journal data for user: ${user.userId}, with journalDate: $journalDate")
        }
    }

    fun editJournal(userToken: String, journal: Journal) {
        // get User Entity
        val user: User = userRepository.findByUserId(getUserIdFromToken(userToken))
        user.journalData.removeIf {it.journalDate == journal.journalDate}
        user.journalData.add(journal)

        userRepository.addUser(user)
    }
}