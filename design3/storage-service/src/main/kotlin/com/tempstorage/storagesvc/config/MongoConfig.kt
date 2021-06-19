package com.tempstorage.storagesvc.config

import com.javawebapp.web.util.ZonedDateTimeReadConverter
import com.javawebapp.web.util.ZonedDateTimeWriteConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions

@Configuration
class MongoConfig {
    @Bean
    fun customConversions(): MongoCustomConversions {
        val converters: MutableList<Converter<*, *>?> = ArrayList()
        converters.add(ZonedDateTimeReadConverter())
        converters.add(ZonedDateTimeWriteConverter())
        return MongoCustomConversions(converters)
    }
}