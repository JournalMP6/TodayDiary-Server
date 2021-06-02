package com.mptsix.todaydiary.data.response

data class UserSealed(
    var userId: String,
    var userName: String,
    var journalCategoryList: List<JournalCategoryResponse>,
    var journalList: List<JournalSealed>
)