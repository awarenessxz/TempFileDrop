package com.tempfiledrop.console.webserver.exception

enum class ErrorCode {
    SERVER_ERROR,
    CLIENT_ERROR,
    MAX_UPLOAD_FILE_SIZE_EXCEED,
    UPLOAD_FAILED,
    DOWNLOAD_FAILED,
    USER_NOT_FOUND,
    FILE_NOT_FOUND,
    NO_MATCHING_RECORD
}