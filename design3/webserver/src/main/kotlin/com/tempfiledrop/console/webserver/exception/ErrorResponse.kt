package com.tempfiledrop.console.webserver.exception

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class ErrorResponse(
        @JsonProperty("message")  val message: String,                   // message
        @JsonProperty("errorCode")  val errorCode: ErrorCode,            // error code
        @JsonProperty("timestamp") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ") val timestamp: Date = Date()   // timestamp
)
