package com.tempstorage.storagesvc.service.notification

import com.tempstorage.storagesvc.service.eventdata.EventDataService
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

    fun sendEvent(message: NotificationMessage) {
        logger.info("Publishing Event (${message.eventType.name}) to $STORAGE_SERVICE_CHANNEL")
        streamBridge.send(STORAGE_SERVICE_CHANNEL, message)
        eventDataService.writeToDB(message)
    }

    fun sendEventwithHeader(message: NotificationMessage) {
        val routingKey = message.bucket
        logger.info("Publishing Event (${message.eventType.name}) to $STORAGE_SERVICE_CHANNEL with router Key ($routingKey)")
        // publish
        streamBridge.send(STORAGE_SERVICE_CHANNEL, MessageBuilder.createMessage(
                message,
                MessageHeaders(mutableMapOf(Pair<String, Any>("routingkey", routingKey)))
        ))
        eventDataService.writeToDB(message)
    }
}