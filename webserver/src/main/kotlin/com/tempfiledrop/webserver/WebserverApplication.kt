package com.tempfiledrop.webserver

import com.tempfiledrop.webserver.service.filestorage.FileStorageService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import javax.annotation.Resource

@SpringBootApplication
class WebserverApplication: CommandLineRunner {
	@Resource
	var storageService: FileStorageService? = null

	@Throws(Exception::class)
	override fun run(vararg args: String?) {
		storageService?.deleteAllFilesInFolder()
		storageService?.initLocalStorage()
	}
}

fun main(args: Array<String>) {
	runApplication<WebserverApplication>(*args)
}
