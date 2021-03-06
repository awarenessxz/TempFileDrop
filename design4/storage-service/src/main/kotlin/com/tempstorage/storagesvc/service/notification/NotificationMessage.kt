package com.tempstorage.storagesvc.service.notification

import java.time.ZonedDateTime

data class NotificationMessage(
        val eventTime: ZonedDateTime,       // Notification Date time
        val eventType: EventType,           // Event Type
        val bucket: String,                 // Bucket
        val objectName: String              // Object Name
)