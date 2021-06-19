package com.tempfiledrop.console.webserver.service.event

data class EventMessage(
        val eventType: String,
        val storageId: String,
        val storagePath: String,
        val storageFiles: String,
        val bucket: String,
        val data: String
)