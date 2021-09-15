package com.tempstorage.console.service.watcher

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="watchlist")
data class WatchList (
    @Id
    val id: String,
    val user: String,
    val schedulerJobName: String,
    val isObjectValid: Boolean
)
