package com.tempstorage.storagesvc.service.notification

data class EventMessage(
        val eventType: String,          // EventType
        val customData: String,         // custom data specified by user
        val storageId: String,
        val originalFilename: String,
        val bucket: String,
        val storageFullPath: String,
)