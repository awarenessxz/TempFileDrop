package com.tempstorage.console.service.watcher

import com.tempstorage.console.service.scheduler.SchedulerJobStatus
import com.tempstorage.console.service.scheduler.SchedulerJobType

data class WatchListJob(
        val jobType: SchedulerJobType,
        val jobName: String,
        val description: String,
        val cronExpression: String,
        var jobStatus: SchedulerJobStatus,
        val isObjectValid: Boolean
)