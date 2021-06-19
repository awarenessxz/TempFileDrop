package com.tempstorage.storagesvc

import com.tempfiledrop.storagesvc.config.StorageSvcProperties
import com.tempfiledrop.storagesvc.service.storage.StorageService
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import javax.annotation.Resource

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(StorageSvcProperties::class)
@OpenAPIDefinition(info = Info(title = "Storage Service API Definitions"))
class StorageServiceApplication: CommandLineRunner {
	@Resource
	var storageService: StorageService? = null

	@Throws(Exception::class)
	override fun run(vararg args: String?) {
		storageService?.initStorage()
	}
}

fun main(args: Array<String>) {
	runApplication<StorageServiceApplication>(*args)
}
