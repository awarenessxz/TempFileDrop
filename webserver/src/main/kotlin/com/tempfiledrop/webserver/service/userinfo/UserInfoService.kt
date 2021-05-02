package com.tempfiledrop.webserver.service.userinfo

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserInfoService(
        private val repository: UserInfoRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserInfoService::class.java)
    }

    fun getUserInfoByUsername(username: String): UserInfo? {
        return repository.findByUsername(username)
    }

    fun attemptToLogin(usernameInput: String, passwordInput: String): UserInfo? {
        return repository.findByUsernameAndPassword(usernameInput, passwordInput)
    }
}