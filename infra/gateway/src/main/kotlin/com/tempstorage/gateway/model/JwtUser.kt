package com.tempstorage.gateway.model

data class JwtUser(
        val name: String,
        val username: String,
        val roles: List<String>,
        val token: String,
        val buckets: List<String> = arrayListOf("tempfiledrop"),
        val routingKeys: List<String> = ArrayList()
)