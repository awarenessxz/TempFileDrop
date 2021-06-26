package com.tempstorage.tempfiledrop.webserver.security

data class StorageAttributes(
        val buckets: List<String>,
        val routingkeys: List<String>,
        val subscribers: List<String>
)