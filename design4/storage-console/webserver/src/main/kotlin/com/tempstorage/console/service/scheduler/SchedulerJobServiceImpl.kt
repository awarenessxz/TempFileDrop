package com.tempstorage.console.service.scheduler

import com.tempstorage.console.job.MonitorObjectCronJob
import com.tempstorage.console.job.MonitorObjectJob
import com.tempstorage.console.util.JobScheduleCreator
import org.quartz.*
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
@Service
class SchedulerJobServiceImpl(
        private val schedulerFactoryBean: SchedulerFactoryBean,
        private val schedulerJobRepository: SchedulerJobRepository,
        private val context: ApplicationContext,
        private val scheduleCreator: JobScheduleCreator
): SchedulerJobService {
    companion object {
        private val logger = LoggerFactory.getLogger(SchedulerJobServiceImpl::class.java)
    }

    @SuppressWarnings("unchecked")
    override fun createSchedulerJob(schedulerJob: SchedulerJob): Boolean {
        val job = processSchedulerJob(schedulerJob)
        return try {
            val scheduler: Scheduler = schedulerFactoryBean.scheduler
            var jobDetail = JobBuilder
                    .newJob(Class.forName(job.jobClass) as Class<out QuartzJobBean?>)
                    .withIdentity(job.jobName, job.jobType.name)
                    .build()
            if (!scheduler.checkExists(jobDetail!!.key)) {
                jobDetail = scheduleCreator.createJob((Class.forName(job.jobClass) as Class<out QuartzJobBean?>), false, context, job.jobName, job.jobType.name)
                val trigger = createTrigger(job)
                scheduler.scheduleJob(jobDetail, trigger)
                updateSchedulerJobStatus(job, SchedulerJobStatus.SCHEDULED)
                logger.info(">>>>>>>> jobName = [${job.jobName}] scheduled.")
                true
            } else {
                logger.error("createSchedulerJob.jobAlreadyExist")
                false
            }
        } catch (e: ClassNotFoundException) {
            logger.error("Class Not Found - ${job.jobClass}", e)
            false
        } catch (e: SchedulerException) {
            logger.error(e.message, e)
            false
        }
    }

    override fun deleteSchedulerJob(jobName: String): Boolean {
        return try {
            val job = getSchedulerJob(jobName)
            schedulerJobRepository.delete(job)
            logger.info(">>>>>>>> jobName = [${job.jobName}] deleted.")
            schedulerFactoryBean.scheduler.deleteJob(JobKey(job.jobName, job.jobType.name))
        } catch (e: SchedulerException) {
            logger.error("Failed to delete job - $jobName", e)
            false
        }
    }

    override fun getAllSchedulerJobs(): List<SchedulerJob> {
        return schedulerJobRepository.findAll()
    }

    override fun getSchedulerJob(jobName: String): SchedulerJob {
        return schedulerJobRepository.findByJobName(jobName) ?: throw Exception("Scheduled Job [$jobName] not found!")
    }

    override fun pauseSchedulerJob(jobName: String): Boolean {
        return try {
            val job = getSchedulerJob(jobName)
            updateSchedulerJobStatus(job, SchedulerJobStatus.PAUSED)
            schedulerFactoryBean.scheduler.pauseJob(JobKey(job.jobName, job.jobType.name))
            logger.info(">>>>>>>> jobName = [${job.jobName}] paused.")
            true
        } catch (e: SchedulerException) {
            logger.error("Failed to pause job - $jobName", e)
            false
        }
    }

    override fun resumeSchedulerJob(jobName: String): Boolean {
        return try {
            val job = getSchedulerJob(jobName)
            updateSchedulerJobStatus(job, SchedulerJobStatus.RESUMED)
            schedulerFactoryBean.scheduler.resumeJob(JobKey(job.jobName, job.jobType.name))
            logger.info(">>>>>>>> jobName = [${job.jobName}] resumed.")
            true
        } catch (e: SchedulerException) {
            logger.error("Failed to resume job - $jobName", e)
            false
        }
    }

    override fun triggerSchedulerJob(jobName: String): Boolean {
        return try {
            val job = getSchedulerJob(jobName)
            schedulerFactoryBean.scheduler.triggerJob(JobKey(job.jobName, job.jobType.name))
            logger.info(">>>>>>>> jobName = [${job.jobName}] triggered immediately.")
            true
        } catch (e: SchedulerException) {
            logger.error("Failed to trigger the job - $jobName", e)
            false
        }
    }

    override fun updateSchedulerJob(schedulerJob: SchedulerJob): Boolean {
        val job = processSchedulerJob(schedulerJob)
        val newTrigger = createTrigger(job)
        return try {
            schedulerFactoryBean.scheduler.rescheduleJob(TriggerKey.triggerKey(job.jobName), newTrigger)
            updateSchedulerJobStatus(job, SchedulerJobStatus.RESCHEDULED)
            logger.info(">>>>>>>> jobName = [${job.jobName}] updated and rescheduled.")
            true
        } catch (e: SchedulerException) {
            logger.error(e.message, e)
            false
        }
    }

    private fun processSchedulerJob(schedulerJob: SchedulerJob): SchedulerJob {
        when (schedulerJob.jobType) {
            SchedulerJobType.MONITOR_OBJECT -> {
                schedulerJob.jobClass = MonitorObjectJob::class.java.name
                schedulerJob.isCronJob = false
                schedulerJob.repeatTime = 1
            }
            SchedulerJobType.MONITOR_OBJECT_CRONJOB -> {
                schedulerJob.jobClass = MonitorObjectCronJob::class.java.name
                schedulerJob.isCronJob = true
            }
        }
        return schedulerJob
    }

    private fun createTrigger(schedulerJob: SchedulerJob): Trigger? {
        return if (schedulerJob.isCronJob) {
            scheduleCreator.createCronTrigger(schedulerJob.jobName, schedulerJob.startTime, schedulerJob.cronExpression, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW)
        } else {
            scheduleCreator.createSimpleTrigger(schedulerJob.jobName, schedulerJob.startTime, schedulerJob.repeatTime, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW)
        }
    }

    private fun updateSchedulerJobStatus(schedulerJob: SchedulerJob, status: SchedulerJobStatus) {
        schedulerJob.jobStatus = status
        schedulerJobRepository.save(schedulerJob)
    }
}