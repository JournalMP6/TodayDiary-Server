package com.mptsix.todaydiary.data.user

import com.mongodb.client.result.DeleteResult
import com.mptsix.todaydiary.data.user.journal.CategoryCountResult
import com.mptsix.todaydiary.error.exception.NotFoundException
import com.mptsix.todaydiary.error.exception.UnknownErrorException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.*
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
    private val journalField: String = "journalData"
    private val userJournalCategoryField: String = "journalData.journalCategory"

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

    // Find category size by user id / by category name
    fun findCategorySizeByUserId(categoryName: String, userId:String):Int {
        // First, match user Id
        val matchOperation: MatchOperation = MatchOperation(Criteria.where(userIdField).`is`(userId))

        // Unwind it
        val unwindOperation: UnwindOperation = Aggregation.unwind(journalField)

        // Match Category
        val matchOperationCategory: MatchOperation = MatchOperation(
            Criteria.where(userJournalCategoryField).`is`(categoryName)
        )

        // Count
        val countOperation: CountOperation = CountOperation("categoryCount")

        val aggregationResult: AggregationResults<CategoryCountResult> = mongoTemplate.aggregate(
            Aggregation.newAggregation(
                matchOperation,
                unwindOperation,
                matchOperationCategory,
                countOperation
            ),
            User::class.java,
            CategoryCountResult::class.java
        )

        if (aggregationResult.mappedResults.isEmpty()) {
            return 0
        }

        return aggregationResult.mappedResults[0].categoryCount
    }

    fun removeUser(userId: String) {
        val removeQuery: Query = Query(
            Criteria.where(userIdField).`is`(userId)
        )
        val removeResult: DeleteResult = mongoTemplate.remove(removeQuery, User::class.java)

        if (!removeResult.wasAcknowledged() || removeResult.deletedCount != 1L) {
            logger.error("Cannot remove user: ${userId}!")
            logger.error("Query: $removeQuery")
            logger.error("Result: $removeResult")
            throw UnknownErrorException("Remove failed for userId: $userId")
        }
    }
}