package com.tempfiledrop.webserver

import com.tempfiledrop.webserver.config.StorageSvcClientProperties
import com.tempfiledrop.webserver.service.event.EventMessage
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.util.function.Consumer

@SpringBootApplication
@EnableConfigurationProperties(StorageSvcClientProperties::class)
class WebserverApplication

fun main(args: Array<String>) {
	runApplication<WebserverApplication>(*args)
}
