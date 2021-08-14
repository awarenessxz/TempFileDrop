package com.tempstorage.storagesvc.service.notification

import java.time.ZonedDateTime

data class NotificationMessage(
        val eventSource: String,            // where the event is from (Minio Cluster or File System)
        val eventTime: ZonedDateTime,       // Notification Date time
        val eventType: EventType,           // Event Type
        val bucket: String,                 // Bucket
        val objectName: String,             // Object Name
        val customEventData: String         // custom event data attached to rest call
)