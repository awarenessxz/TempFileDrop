package com.tempfiledrop.webserver.util.exception

import org.springframework.http.HttpStatus
import java.lang.Exception

open class ApiException(
        message: String,
        val errorType: ErrorTypes? = ErrorTypes.UNKNOWN,
        val status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR
): Exception(message)