package com.tempstorage.storagesvc.security

data class StorageAttributes(
        val buckets: List<String>,
        val routingkeys: List<String>,
        val subscribers: List<String>
)