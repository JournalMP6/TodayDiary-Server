package com.mptsix.todaydiary.data

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("user")
data class User(
    @Id
    var id: ObjectId = ObjectId(),
    // For Logging-In
    var userId: String,
    var userPassword: String,

    // For Registration
    var userName: String,
    var userDateOfBirth: String,
    var userPasswordQuestion: String,
    var userPasswordAnswer: String
)