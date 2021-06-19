package com.tempstorage.tempfiledrop.webserver.util

import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseExtractor
import java.io.IOException
import java.io.InputStream

class StreamResponseExtractor(private val reader: StreamResourceReader) : ResponseExtractor<InputStream?> {
    @Throws(IOException::class)
    override fun extractData(response: ClientHttpResponse): InputStream? {
        reader.read(response.body, response.headers)
        return null
    }
}