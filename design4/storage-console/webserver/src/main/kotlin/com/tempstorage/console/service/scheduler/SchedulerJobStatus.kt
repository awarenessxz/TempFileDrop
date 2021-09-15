package com.tempstorage.console.service.scheduler

enum class SchedulerJobStatus {
    COMPLETED,
    COMPLETED_WITH_ERROR,
    PAUSED,
    PENDING,
    RESCHEDULED,
    RESUMED,
    RUNNING,
    SCHEDULED
}