package com.tempfiledrop.webserver.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class StorageSvcRestConfig {
    @Bean
    fun storageSvcRestTemplate(): RestTemplate {
        val requestFactory = SimpleClientHttpRequestFactory()
        requestFactory.setBufferRequestBody(false) // required to prevent out of memory issue when uploading files with rest template
        return RestTemplate(requestFactory)
    }
}