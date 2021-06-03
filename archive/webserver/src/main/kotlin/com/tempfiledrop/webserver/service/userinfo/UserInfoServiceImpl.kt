package com.tempfiledrop.webserver.service.userinfo

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserInfoServiceImpl(
        private val repository: UserInfoRepository
): UserInfoService {
    companion object {
        private val logger = LoggerFactory.getLogger(UserInfoServiceImpl::class.java)
    }

    override fun getUserInfoByUsername(username: String): UserInfo? {
        return repository.findByUsername(username)
    }

    override fun attemptToLogin(usernameInput: String, passwordInput: String): UserInfo? {
        return repository.findByUsernameAndPassword(usernameInput, passwordInput)
    }
}