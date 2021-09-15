package com.tempstorage.console.service.scheduler

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection="scheduled_jobs")
data class SchedulerJob(
        @Id
        val jobId: String = "",
        val jobType: SchedulerJobType,
        val jobName: String,
        val description: String,
        val cronExpression: String = "",
        val startTime: Date = Date(),
        var jobStatus: SchedulerJobStatus = SchedulerJobStatus.PENDING,
        var repeatTime: Long = 0,
        var isCronJob: Boolean = false,
        var jobClass: String? = null
)
