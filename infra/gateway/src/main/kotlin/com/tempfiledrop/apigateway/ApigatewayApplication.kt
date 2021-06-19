package com.tempfiledrop.apigateway

import com.tempfiledrop.apigateway.config.GatewayProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(GatewayProperties::class)
class ApigatewayApplication

fun main(args: Array<String>) {
	runApplication<ApigatewayApplication>(*args)
}
