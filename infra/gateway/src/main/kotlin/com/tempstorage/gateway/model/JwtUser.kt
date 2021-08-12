package com.tempstorage.gateway.model

data class JwtUser(
        val name: String,
        val username: String,
        val roles: List<String>,
        val token: String
)