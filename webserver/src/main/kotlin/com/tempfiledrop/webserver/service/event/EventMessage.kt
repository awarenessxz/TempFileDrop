package com.tempfiledrop.webserver.service.event

data class EventMessage(
        val storageId: String,
        val storagePath: String,
        val storageFiles: String,
        val bucket: String,
        val data: String
)