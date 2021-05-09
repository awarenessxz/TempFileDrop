package com.tempfiledrop.storagesvc

import com.tempfiledrop.storagesvc.config.StorageSvcProperties
import com.tempfiledrop.storagesvc.service.storage.StorageService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import javax.annotation.Resource

@SpringBootApplication
@EnableConfigurationProperties(StorageSvcProperties::class)
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
