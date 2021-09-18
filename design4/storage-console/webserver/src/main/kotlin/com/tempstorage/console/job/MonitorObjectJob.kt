package com.tempstorage.console.job

import com.tempstorage.console.service.watcher.WatcherService
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component

@Component
class MonitorObjectJob: QuartzJobBean() {
    companion object {
        private val logger = LoggerFactory.getLogger(MonitorObjectJob::class.java)
    }

    @Autowired
    private lateinit var watcherService: WatcherService

    @Throws(JobExecutionException::class)
    override fun executeInternal(context: JobExecutionContext) {
        logger.info("[START] Triggering Monitor Object Job - ${context.jobDetail.key.name}")
        watcherService.triggerWatcher(context.jobDetail.key.name)
        logger.info("[END] Monitor Object Job - ${context.jobDetail.key.name}")
    }
}