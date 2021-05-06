package com.tempfiledrop.storagesvc.exception

import java.util.*

data class ErrorResponse(
        val timestamp: Date,                   // timestamp
        val message: String,                   // message
        val errorCode: ErrorCode               // error code
)
