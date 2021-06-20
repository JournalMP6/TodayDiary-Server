package com.mptsix.todaydiary.data.request

data class PasswordChangeRequest(
    var previousPassword: String,
    var userPassword: String // 바꾸고 싶은 비밀번호
)