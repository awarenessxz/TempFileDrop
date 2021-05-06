package com.tempfiledrop.webserver

import com.tempfiledrop.webserver.config.ServerProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ServerProperties::class)
class WebserverApplication

fun main(args: Array<String>) {
	runApplication<WebserverApplication>(*args)
}
