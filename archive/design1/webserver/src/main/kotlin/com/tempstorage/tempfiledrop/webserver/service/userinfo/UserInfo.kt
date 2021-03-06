package com.tempstorage.tempfiledrop.webserver.service.userinfo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime

@Document(collection="users")
data class UserInfo(
        @Id val id: String?,                  // mongoDB ID
        val username: String,
        val password: String,
        val creationDate: ZonedDateTime
)