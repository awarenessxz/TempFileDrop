package com.tempstorage.console.controller

import com.tempstorage.console.service.scheduler.SchedulerJob
import com.tempstorage.console.service.scheduler.SchedulerJobService
import com.tempstorage.console.service.watcher.WatchList
import com.tempstorage.console.service.watcher.WatchListJob
import com.tempstorage.console.service.watcher.WatcherService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/console/watch")
class WatcherController(
        private val schedulerJobService: SchedulerJobService,
        private val watcherService: WatcherService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(WatcherController::class.java)
    }

    @GetMapping("/list")
    fun getAllJobs(): ResponseEntity<List<SchedulerJob>> {
        val response = schedulerJobService.getAllSchedulerJobs()
        return ResponseEntity(response, HttpStatus.OK)
    }

    @GetMapping("/list/{user}")
    fun getAllJobsForUser(@PathVariable("user") user: String): ResponseEntity<List<WatchListJob>> {
        val userWatchList = watcherService.getUserWatchList(user)
        val response = userWatchList.map {
            val job = schedulerJobService.getSchedulerJob(it.schedulerJobName)
            WatchListJob(job.jobType, job.jobName, job.description, job.cronExpression, job.jobStatus, it.isObjectValid)
        }
        return ResponseEntity(response, HttpStatus.OK)
    }

    @GetMapping("/{jobName}")
    fun getSchedulerJob(@PathVariable("jobName") jobName: String): ResponseEntity<SchedulerJob> {
        val response = schedulerJobService.getSchedulerJob(jobName)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @DeleteMapping("/{jobName}")
    fun deleteSchedulerJob(@PathVariable("jobName") jobName: String): ResponseEntity<String> {
        val result = schedulerJobService.deleteSchedulerJob(jobName)
        val response = if (result) "$jobName deleted successfully" else "Failed to delete $jobName"
        return ResponseEntity(response, HttpStatus.OK)
    }

    @PostMapping("/create/{user}")
    fun createNewSchedulerJob(@PathVariable("user") user: String, @RequestBody schedulerJob: SchedulerJob): ResponseEntity<String> {
        logger.info(">>>> Creating New Monitor Job = $schedulerJob for $user")
        val result = schedulerJobService.createSchedulerJob(schedulerJob)
        if (result) {
            watcherService.saveUserWatchList(WatchList("", user, schedulerJob.jobName, false))
        }
        val response = if (result) "${schedulerJob.jobName} created successfully" else "Failed to create ${schedulerJob.jobName}"
        return ResponseEntity(response, HttpStatus.OK)
    }

    @PostMapping("/trigger/{jobName}")
    fun triggerSchedulerJob(@PathVariable("jobName") jobName: String): ResponseEntity<String> {
        val result = schedulerJobService.triggerSchedulerJob(jobName)
        val response = if (result) "$jobName created successfully" else "Failed to create $jobName"
        return ResponseEntity(response, HttpStatus.OK)
    }

    @PutMapping("/pause/{jobName}")
    fun pauseSchedulerJob(@PathVariable("jobName") jobName: String): ResponseEntity<String> {
        val result = schedulerJobService.pauseSchedulerJob(jobName)
        val response = if (result) "$jobName paused successfully" else "Failed to pause $jobName"
        return ResponseEntity(response, HttpStatus.OK)
    }

    @PutMapping("/resume/{jobName}")
    fun resumeSchedulerJob(@PathVariable("jobName") jobName: String): ResponseEntity<String> {
        val result = schedulerJobService.resumeSchedulerJob(jobName)
        val response = if (result) "$jobName resumed successfully" else "Failed to resume $jobName"
        return ResponseEntity(response, HttpStatus.OK)
    }

    @PutMapping("/update")
    fun updateSchedulerJob(@RequestBody schedulerJob: SchedulerJob): ResponseEntity<String> {
        val result = schedulerJobService.updateSchedulerJob(schedulerJob)
        val response = if (result) "${schedulerJob.jobName} updated successfully" else "Failed to update ${schedulerJob.jobName}"
        return ResponseEntity(response, HttpStatus.OK)
    }
}
