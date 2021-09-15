package com.tempstorage.console.service.scheduler

interface SchedulerJobService {
    fun createSchedulerJob(schedulerJob: SchedulerJob): Boolean
    fun deleteSchedulerJob(jobName: String): Boolean
    fun getAllSchedulerJobs(): List<SchedulerJob>
    fun getSchedulerJob(jobName: String): SchedulerJob
    fun pauseSchedulerJob(jobName: String): Boolean
    fun resumeSchedulerJob(jobName: String): Boolean
    fun triggerSchedulerJob(jobName: String): Boolean
    fun updateSchedulerJob(schedulerJob: SchedulerJob): Boolean
}