package com.mptsix.todaydiary.data.response

import com.mptsix.todaydiary.data.user.journal.JournalCategory

data class JournalCategoryResponse(
    var category: JournalCategory,
    var count: Int
)
