package com.javawebapp.web.util

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

@ReadingConverter
class ZonedDateTimeReadConverter: Converter<Date, ZonedDateTime> {
    override fun convert(source: Date): ZonedDateTime? {
        return source.toInstant().atZone(ZoneOffset.UTC)
    }
}