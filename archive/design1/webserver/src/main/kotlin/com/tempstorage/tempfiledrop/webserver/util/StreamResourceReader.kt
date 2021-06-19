package com.tempstorage.tempfiledrop.webserver.util

import org.apache.commons.io.IOUtils
import org.springframework.http.HttpHeaders
import java.io.IOException
import java.io.InputStream
import javax.servlet.http.HttpServletResponse

class StreamResourceReader(private val response: HttpServletResponse) {
    fun read(content: InputStream, headers: HttpHeaders) {
        try {
            response.contentType = headers.contentType.toString()
            response.setContentLengthLong(headers.contentLength)
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, headers.contentDisposition.toString())
            IOUtils.copy(content, response.outputStream)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }
}