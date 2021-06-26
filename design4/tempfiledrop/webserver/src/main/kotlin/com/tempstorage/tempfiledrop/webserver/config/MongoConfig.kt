package com.tempstorage.tempfiledrop.webserver.config

import com.tempstorage.tempfiledrop.webserver.util.ZonedDateTimeReadConverter
import com.tempstorage.tempfiledrop.webserver.util.ZonedDateTimeWriteConverter
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