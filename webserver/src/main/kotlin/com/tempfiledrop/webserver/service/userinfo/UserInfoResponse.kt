package com.tempfiledrop.webserver.service.userinfo

data class UserInfoResponse(
    val userExists: Boolean,
    val userToken: String
)
