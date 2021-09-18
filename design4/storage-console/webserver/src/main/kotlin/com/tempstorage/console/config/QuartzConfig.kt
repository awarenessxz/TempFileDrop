package com.tempstorage.console.config

import org.springframework.boot.autoconfigure.quartz.QuartzProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import java.util.*

@Configuration
class QuartzConfig(
        private val quartzProperties: QuartzProperties,
        private val applicationContext: ApplicationContext
) {

    @Bean
    fun schedulerFactoryBean(): SchedulerFactoryBean {
        val jobFactory = SchedulerJobFactory()
        jobFactory.setApplicationContext(applicationContext)

        val properties = Properties()
        properties.putAll(quartzProperties.properties)

        val factory = SchedulerFactoryBean()
        factory.setOverwriteExistingJobs(true)
        factory.setWaitForJobsToCompleteOnShutdown(true)
        factory.setQuartzProperties(properties)
        factory.setJobFactory(jobFactory)
        return factory
    }
}