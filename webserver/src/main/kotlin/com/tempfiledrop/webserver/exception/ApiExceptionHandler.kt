package com.tempfiledrop.webserver.exception

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.util.*

@ControllerAdvice
class ApiExceptionHandler: ResponseEntityExceptionHandler() {

    // Global Exception Handler
    @ExceptionHandler(value = [(Exception::class)])
    fun handleAnyException(ex: Exception, request: WebRequest): ResponseEntity<Any> {
        val errorMsgDescription = if (ex.localizedMessage == null) ex.toString() else ex.localizedMessage
        val errorMsg = ErrorResponse(Date(), errorMsgDescription, ErrorCode.SERVER_ERROR)
        return ResponseEntity(errorMsg, HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    // Max Upload File Size Exceed Exception
    @ExceptionHandler(value = [(MaxUploadSizeExceededException::class)])
    fun handleMaxUploadSizeException(ex: MaxUploadSizeExceededException): ResponseEntity<Any> {
        val errorMsg = ErrorResponse(Date(), "Upload Max File Size Exceeded!", ErrorCode.MAX_UPLOAD_FILE_SIZE_EXCEED)
        return ResponseEntity(errorMsg, HttpHeaders(), HttpStatus.BAD_REQUEST)
    }

    // Api Exception Handler
    @ExceptionHandler(value = [(ApiException::class)])
    fun handleApiException(ex: ApiException, request: WebRequest): ResponseEntity<Any> {
        val errorMsgDescription = if (ex.localizedMessage == null) ex.toString() else ex.localizedMessage
        val errorMsg = ErrorResponse(Date(), errorMsgDescription, ex.errorCode)
        return ResponseEntity(errorMsg, HttpHeaders(), ex.status)
    }
}