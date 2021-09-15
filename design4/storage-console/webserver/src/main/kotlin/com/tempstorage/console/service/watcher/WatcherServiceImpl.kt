package com.tempstorage.console.service.watcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WatcherServiceImpl(
        private val watchListRepository: WatchListRepository
): WatcherService {
    companion object {
        private val logger = LoggerFactory.getLogger(WatcherServiceImpl::class.java)
    }

    override fun deleteUserWatchList(watchlist: WatchList) {
        watchListRepository.delete(watchlist)
    }

    override fun getUserWatchList(user: String): List<WatchList> {
        return watchListRepository.findByUser(user)
    }

    override fun saveUserWatchList(watchlist: WatchList) {
        watchListRepository.save(watchlist)
    }

    override fun triggerWatcher(jobName: String) {
        logger.info("Triggering $jobName")
    }
}
