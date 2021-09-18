package com.tempstorage.console.service.scheduler

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

enum class SchedulerJobType {
    @JsonProperty("MONITOR_OBJECT") MONITOR_OBJECT,
    @JsonProperty("MONITOR_OBJECT_CRONJOB") MONITOR_OBJECT_CRONJOB
    ;

    companion object {
        @JsonCreator @JvmStatic fun fromText(value: String): SchedulerJobType = values().first { it.name == value }
    }
}
