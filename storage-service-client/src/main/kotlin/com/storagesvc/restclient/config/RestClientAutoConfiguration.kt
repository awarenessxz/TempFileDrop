package com.storagesvc.restclient.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(RestClientProperties::class)
class RestClientAutoConfiguration(
        private val restClientProps: RestClientProperties
) {

}
