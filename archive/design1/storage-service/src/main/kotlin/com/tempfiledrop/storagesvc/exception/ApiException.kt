package com.tempfiledrop.storagesvc.exception

import org.springframework.http.HttpStatus
import java.lang.Exception

open class ApiException(
        message: String,
        val errorCode: ErrorCode = ErrorCode.SERVER_ERROR,
        val status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR
): Exception(message)