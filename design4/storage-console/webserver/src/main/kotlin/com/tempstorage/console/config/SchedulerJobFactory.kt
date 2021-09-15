package com.tempstorage.console.config

import org.quartz.spi.TriggerFiredBundle
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.scheduling.quartz.SpringBeanJobFactory

class SchedulerJobFactory: SpringBeanJobFactory(), ApplicationContextAware {
    private lateinit var beanFactory: AutowireCapableBeanFactory

    override fun setApplicationContext(context: ApplicationContext) {
        beanFactory = context.autowireCapableBeanFactory
    }

    @Throws(Exception::class)
    override fun createJobInstance(bundle: TriggerFiredBundle): Any {
        val job = super.createJobInstance(bundle)
        beanFactory.autowireBean(job)
        return job
    }
}