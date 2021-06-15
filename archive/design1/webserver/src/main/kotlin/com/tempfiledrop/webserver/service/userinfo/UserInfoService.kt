package com.tempfiledrop.webserver.service.userinfo

interface UserInfoService {
    fun getUserInfoByUsername(username: String): UserInfo?
    fun attemptToLogin(usernameInput: String, passwordInput: String): UserInfo?
}