package com.tempstorage.tempfiledrop.webserver.util

object FileUtil {
    fun getUsernameFromObjectName(objectName: String): String {
        return objectName.substringBeforeLast("/")
    }

    fun getOriginalFilenameFromObjectName(objectName: String): String {
        return objectName.substringAfterLast("/")
    }
}