package com.tempfiledrop.storagesvc

import com.tempfiledrop.storagesvc.config.StorageSvcProperties
import com.tempfiledrop.storagesvc.service.filestorage.FileStorageService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import javax.annotation.Resource

@SpringBootApplication
@EnableConfigurationProperties(StorageSvcProperties::class)
class StorageServiceApplication: CommandLineRunner {
	@Resource
	var storageService: FileStorageService? = null

	@Throws(Exception::class)
	override fun run(vararg args: String?) {
		storageService?.deleteAllFilesInFolder()
		storageService?.initLocalStorage()
	}
}

fun main(args: Array<String>) {
	runApplication<StorageServiceApplication>(*args)
}
