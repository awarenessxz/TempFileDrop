package com.tempfiledrop.webserver.util.exception

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.util.*

@ControllerAdvice
class ApiExceptionHandler: ResponseEntityExceptionHandler() {

    // Global Exception Handler
    @ExceptionHandler(value = [(Exception::class)])
    fun handleAnyException(ex: Exception, request: WebRequest): ResponseEntity<Any> {
        val errorMsgDescription = if (ex.localizedMessage == null) ex.toString() else ex.localizedMessage
        val errorMsg = ErrorMessage(Date(), errorMsgDescription)
        return ResponseEntity(errorMsg, HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    // Api Exception Handler
    @ExceptionHandler(value = [(ApiException::class)])
    fun handleApiException(ex: ApiException, request: WebRequest): ResponseEntity<Any> {
        val errorMsgDescription = if (ex.localizedMessage == null) ex.toString() else ex.localizedMessage
        val errorMsg = ErrorMessage(Date(), errorMsgDescription, ex.errorType)
        return ResponseEntity(errorMsg, HttpHeaders(), ex.status)
    }
}