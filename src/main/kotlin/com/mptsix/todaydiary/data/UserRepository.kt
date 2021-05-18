package com.mptsix.todaydiary.data

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
@Repository
class UserRepository(
    private val mongoTemplate: MongoTemplate
) {
    // Logger
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    // Field
    private val userIdField: String = "userId"
    private val userNameField: String = "userName"

    // For getting query object for find where 'field' = fieldTargetValue
    private fun getQueryForFindBy(fieldName: String, fieldTargetValue: String): Query {
        return Query().apply {
            addCriteria(
                Criteria.where(fieldName).`is`(fieldTargetValue)
            )
        }
    }

    // For getting user object with specific query: so only one result
    private fun findOneByQuery(fieldName: String, fieldTargetValue: String): User {
        val query: Query = getQueryForFindBy(fieldName, fieldTargetValue)

        // Find it
        return mongoTemplate.findOne<User>(query) ?: run {
            logger.error("Cannot get data!")
            logger.error("Query: $query")
            throw NullPointerException("Cannot get userdata. Query: $query")
        }
    }

    // Add or update user
    fun addUser(user: User): User = mongoTemplate.save(user)

    // Find User by ID[User ID]
    fun findByUserId(userId: String): User = findOneByQuery(userIdField, userId)

    // Find User by User Name
    fun findByUserName(userName: String): User = findOneByQuery(userNameField, userName)
}