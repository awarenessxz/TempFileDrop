package com.tempstorage.console.service.scheduler

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection="scheduled_jobs")
data class SchedulerJob(
        @Id
        val jobId: String = "",
        @JsonProperty("jobType") val jobType: SchedulerJobType,
        @JsonProperty("jobName") val jobName: String,
        @JsonProperty("description") val description: String,
        @JsonProperty("cronExpression") val cronExpression: String = "",
        @JsonProperty("startTime") val startTime: Date = Date(),
        var jobStatus: SchedulerJobStatus = SchedulerJobStatus.PENDING,
        var repeatTime: Long = 0,
        var isCronJob: Boolean = false,
        var jobClass: String? = null
)
