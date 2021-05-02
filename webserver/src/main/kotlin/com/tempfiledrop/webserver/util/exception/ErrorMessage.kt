package com.tempfiledrop.webserver.util.exception

import java.util.*

data class ErrorMessage(
        val timestamp: Date,                    // timestamp
        val message: String,                    // message
        val errorType: ErrorTypes? = null       // Enums
)
