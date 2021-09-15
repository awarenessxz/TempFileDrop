package com.tempstorage.console.service.watcher

import org.springframework.data.mongodb.repository.MongoRepository

interface WatchListRepository: MongoRepository<WatchList, String> {
    fun findByUser(user: String): List<WatchList>
}