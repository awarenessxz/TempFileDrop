package com.tempstorage.tempfiledrop.webserver

import com.tempfiledrop.webserver.config.StorageSvcClientProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(StorageSvcClientProperties::class)
class WebserverApplication

fun main(args: Array<String>) {
	runApplication<WebserverApplication>(*args)
}
