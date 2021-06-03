package com.tempfiledrop.storagesvc.service.event

import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfo
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component

@Component
class RabbitMQProducer(
        private val streamBridge: StreamBridge
) {
    companion object {
        private const val FILES_DOWNLOADED_OUTPUT_CHANNEL = "filesDownloadedChannel-out-0"
        private const val FILES_UPLOADED_OUTPUT_CHANNEL = "filesUploadedChannel-out-0"
    }

    private fun getBinding(eventType: EventType): String {
        return when (eventType) {
            EventType.FILE_DOWNLOADED -> FILES_DOWNLOADED_OUTPUT_CHANNEL
            EventType.FILE_UPLOADED -> FILES_UPLOADED_OUTPUT_CHANNEL
        }
    }

    fun sendEvent(eventType: EventType, storageInfo: StorageInfo, data: String) {
        val message = EventMessage(storageInfo.id.toString(), storageInfo.storagePath, storageInfo.filenames, storageInfo.bucketName, data)
        val binding = getBinding(eventType)
        streamBridge.send(binding, message)
    }

    fun sendEventwithHeader(eventType: EventType, storageInfo: StorageInfo, data: String, routingKey: String = "#") {
        val message = EventMessage(storageInfo.id.toString(), storageInfo.storagePath, storageInfo.filenames, storageInfo.bucketName, data)
        val binding = getBinding(eventType)
        streamBridge.send(binding, MessageBuilder.createMessage(
                message,
                MessageHeaders(mutableMapOf(Pair<String, Any>("routingkey", routingKey)))
        ))
    }
}