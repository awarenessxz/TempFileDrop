package com.tempfiledrop.webserver.util

import com.tempfiledrop.webserver.exception.ApiException
import com.tempfiledrop.webserver.exception.ErrorCode
import org.springframework.http.HttpStatus
import java.time.ZoneOffset
import java.time.ZonedDateTime

object StorageUtils {
    /* *
     * Calculate the expiry date time
     * @Param
     *      expiryPeriodIdx: 0 = 1 hour, 1 = 1 day, 2 = 1 week
     */
    fun processExpiryPeriod(expiryPeriodIdx: Int): ZonedDateTime {
        val now = ZonedDateTime.now(ZoneOffset.UTC)
        return when(expiryPeriodIdx) {
            0 -> now.plusHours(1)
            1 -> now.plusDays(1)
            2 -> now.plusWeeks(1)
            3 -> now.plusMonths(1)
            else -> throw ApiException("Invalid Expiry Period!", ErrorCode.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}