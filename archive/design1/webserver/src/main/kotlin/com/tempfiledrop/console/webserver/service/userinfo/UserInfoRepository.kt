package com.tempfiledrop.console.webserver.service.userinfo

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserInfoRepository: MongoRepository<UserInfo, String> {
    fun findByOrderByCreationDateDesc(): List<UserInfo>
    fun findByUsername(username: String): UserInfo?
    fun findByUsernameAndPassword(username: String, password: String): UserInfo?
}