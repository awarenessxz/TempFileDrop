package com.storagesvc.restclient.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "storagesvc.restclient")
data class RestClientProperties(
        val name: String
)