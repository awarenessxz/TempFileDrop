package com.tempstorage.tempfiledrop.webserver.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ApiExceptionHandler: ResponseEntityExceptionHandler() {
    companion object {
        private val logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)
    }

    // Global Exception Handler
    @ExceptionHandler(value = [(Exception::class)])
    fun handleAnyException(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorMsgDescription = if (ex.localizedMessage == null) ex.toString() else ex.localizedMessage
        val errorMsg = ErrorResponse(errorMsgDescription, ErrorCode.SERVER_ERROR)
        logger.error("Global Exception!! $errorMsg")
        return ResponseEntity(errorMsg, HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    // Max Upload File Size Exceed Exception
    @ExceptionHandler(value = [(MaxUploadSizeExceededException::class)])
    fun handleMaxUploadSizeException(ex: MaxUploadSizeExceededException): ResponseEntity<ErrorResponse> {
        val errorMsg = ErrorResponse("Upload Max File Size Exceeded!", ErrorCode.MAX_UPLOAD_FILE_SIZE_EXCEED)
        logger.error("Max Upload Size Exceeded Exception!! $errorMsg")
        return ResponseEntity(errorMsg, HttpHeaders(), HttpStatus.BAD_REQUEST)
    }

    // Api Exception Handler
    @ExceptionHandler(value = [(ApiException::class)])
    fun handleApiException(ex: ApiException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorMsgDescription = if (ex.localizedMessage == null) ex.toString() else ex.localizedMessage
        val errorMsg = ErrorResponse(errorMsgDescription, ex.errorCode)
        logger.error("API Exception!! $errorMsg")
        return ResponseEntity(errorMsg, HttpHeaders(), ex.status)
    }
}