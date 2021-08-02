package com.tempstorage.storagesvc.service.notification

import com.tempstorage.storagesvc.service.eventdata.EventDataService
import com.tempstorage.storagesvc.service.storageinfo.StorageInfo
import com.tempstorage.storagesvc.util.StorageUtils
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component

@Component
class RabbitMQProducer(
        private val streamBridge: StreamBridge,
        private val eventDataService: EventDataService,
) {
    companion object {
        private const val STORAGE_SERVICE_CHANNEL = "storageSvcChannel-out-0"
        private val logger = LoggerFactory.getLogger(RabbitMQProducer::class.java)
    }

    fun sendEvent(eventType: EventType, storageInfo: StorageInfo, data: String) {
        logger.info("Publishing Event ($eventType.name) to $STORAGE_SERVICE_CHANNEL")
        val message = EventMessage(eventType.name, storageInfo.id.toString(), storageInfo.storagePath, storageInfo.filenames, storageInfo.bucket, data)
        streamBridge.send(STORAGE_SERVICE_CHANNEL, message)
        eventDataService.writeToDB(message)
    }

    fun sendEventwithHeader(eventType: EventType, storageInfo: StorageInfo, data: String, routingKey: String, requiresAuth: Boolean) {
        val message = EventMessage(eventType.name, storageInfo.id.toString(), storageInfo.storagePath, storageInfo.filenames, storageInfo.bucket, data)
        logger.info("Publishing Event (${eventType.name}) to $STORAGE_SERVICE_CHANNEL with router Key ($routingKey)")
        // validate routing key
        if (requiresAuth) {
            StorageUtils.validateRoutingKeysWithJwtToken(routingKey)
        }
        // publish
        streamBridge.send(STORAGE_SERVICE_CHANNEL, MessageBuilder.createMessage(
                message,
                MessageHeaders(mutableMapOf(Pair<String, Any>("routingkey", routingKey)))
        ))
        eventDataService.writeToDB(message)
    }
}