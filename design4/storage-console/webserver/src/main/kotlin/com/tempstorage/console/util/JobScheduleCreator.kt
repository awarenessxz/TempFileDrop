package com.tempstorage.console.util

import org.quartz.CronTrigger
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.SimpleTrigger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.quartz.CronTriggerFactoryBean
import org.springframework.scheduling.quartz.JobDetailFactoryBean
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean
import org.springframework.stereotype.Component
import java.text.ParseException
import java.util.*

@Component
class JobScheduleCreator {
    companion object {
        private val logger = LoggerFactory.getLogger(JobScheduleCreator::class.java)
    }

    fun createJob(jobClass: Class<out QuartzJobBean?>, isDurable: Boolean, context: ApplicationContext, jobName: String, jobGroup: String): JobDetail? {
        val factoryBean = JobDetailFactoryBean()
        factoryBean.setJobClass(jobClass)
        factoryBean.setDurability(isDurable)
        factoryBean.setApplicationContext(context)
        factoryBean.setName(jobName)
        factoryBean.setGroup(jobGroup)

        // set job data map
        val jobDataMap = JobDataMap()
        jobDataMap[jobName + jobGroup] = jobClass.name
        factoryBean.jobDataMap = jobDataMap
        factoryBean.afterPropertiesSet()
        return factoryBean.getObject()
    }

    fun createCronTrigger(triggerName: String, startTime: Date, cronExpression: String, misFireInstruction: Int): CronTrigger? {
        val factoryBean = CronTriggerFactoryBean()
        factoryBean.setName(triggerName)
        factoryBean.setStartTime(startTime)
        factoryBean.setCronExpression(cronExpression)
        factoryBean.setMisfireInstruction(misFireInstruction)
        try {
            factoryBean.afterPropertiesSet()
        } catch (e: ParseException) {
            logger.error(e.message, e)
        }
        return factoryBean.getObject()
    }

    fun createSimpleTrigger(triggerName: String, startTime: Date, repeatTime: Long, misFireInstruction: Int): SimpleTrigger? {
        val factoryBean = SimpleTriggerFactoryBean()
        factoryBean.setName(triggerName)
        factoryBean.setStartTime(startTime)
        factoryBean.setRepeatInterval(repeatTime)
        factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY)
        factoryBean.setMisfireInstruction(misFireInstruction)
        factoryBean.afterPropertiesSet()
        return factoryBean.getObject()
    }
}