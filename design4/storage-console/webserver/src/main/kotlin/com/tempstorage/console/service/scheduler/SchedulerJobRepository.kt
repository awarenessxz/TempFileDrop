package com.tempstorage.console.service.scheduler

import org.springframework.data.mongodb.repository.MongoRepository

interface SchedulerJobRepository: MongoRepository<SchedulerJob, String> {
    fun findByJobName(jobName: String): SchedulerJob?
}
