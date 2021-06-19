package com.tempfiledrop.console.webserver.service.userinfo

data class UserInfoResponse(
    val userExists: Boolean,
    val userToken: String
)
