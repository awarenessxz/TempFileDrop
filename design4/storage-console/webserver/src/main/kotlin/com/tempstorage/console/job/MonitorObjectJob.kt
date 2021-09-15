package com.tempstorage.console.job

import com.tempstorage.console.service.watcher.WatcherService
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.quartz.QuartzJobBean

class MonitorObjectJob(
        private val watcherService: WatcherService
): QuartzJobBean() {
    companion object {
        private val logger = LoggerFactory.getLogger(MonitorObjectJob::class.java)
    }

    override fun executeInternal(context: JobExecutionContext) {
        logger.info("[START] Triggering Monitor Object Job - ${context.jobDetail.key.name}")
        watcherService.triggerWatcher(context.jobDetail.key.name)
        logger.info("[END] Monitor Object Job - ${context.jobDetail.key.name}")
    }
}