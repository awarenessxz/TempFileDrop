package com.tempstorage.console.service.watcher

interface WatcherService {
    fun deleteUserWatchList(watchlist: WatchList)
    fun getUserWatchList(user: String): List<WatchList>
    fun saveUserWatchList(watchlist: WatchList)
    fun triggerWatcher(jobName: String)
}