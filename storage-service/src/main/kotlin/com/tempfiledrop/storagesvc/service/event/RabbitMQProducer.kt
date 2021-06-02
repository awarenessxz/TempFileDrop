package com.tempfiledrop.storagesvc.service.event

import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.stereotype.Component

@Component
class RabbitMQProducer(
        private val streamBridge: StreamBridge
) {
    fun sendEvent(eventType: EventType) {
        val message = EventMessage("SOMETHING")
        val binding = when (eventType) {
            EventType.FILE_DOWNLOADED -> "filesDownloadedChannel-out-0"
            EventType.FILE_UPLOADED -> "filesUploadedChannel-out-0"
        }
        streamBridge.send(binding, message)
    }
}