package com.tempstorage.storagesvc.exception

enum class ErrorCode {
    SERVER_ERROR,
    CLIENT_ERROR,
    MAX_UPLOAD_FILE_SIZE_EXCEED,
    BUCKET_NOT_FOUND,
    FILE_NOT_FOUND,
    UPLOAD_FAILED,
    DOWNLOAD_DENIED
}