package com.tempfiledrop.webserver.util

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import java.time.ZonedDateTime
import java.util.*

@WritingConverter
class ZonedDateTimeWriteConverter: Converter<ZonedDateTime, Date> {
    override fun convert(source: ZonedDateTime): Date? {
        return Date.from(source.toInstant())
    }
}