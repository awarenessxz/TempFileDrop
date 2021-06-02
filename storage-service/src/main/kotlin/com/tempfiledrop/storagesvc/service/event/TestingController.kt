package com.tempfiledrop.storagesvc.service.event

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/testing")
class TestingController(
        private val producer: RabbitMQProducer
) {
    companion object {
        private val logger = LoggerFactory.getLogger(TestingController::class.java)
    }

    @GetMapping("/download")
    fun testingProducer(): ResponseEntity<Void> {
        producer.sendEvent(EventType.FILE_DOWNLOADED)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/upload")
    fun testingProducer2(): ResponseEntity<Void> {
        producer.sendEvent(EventType.FILE_UPLOADED)
        return ResponseEntity.noContent().build()
    }
}