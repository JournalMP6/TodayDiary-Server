package com.mptsix.todaydiary.data.user.journal

data class Journal(
    var mainJournalContent: String, // Journal Content[내용]
    var journalLocation: String, // "위도, 경도"
    var journalCategory: JournalCategory, // Category
    var journalWeather: String, // Weather
    var journalDate: Long, // Timestamp
    var journalImage: JournalImage = JournalImage() // Image attribute, default null
)