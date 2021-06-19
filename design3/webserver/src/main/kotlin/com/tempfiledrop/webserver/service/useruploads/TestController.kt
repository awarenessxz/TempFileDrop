package com.tempfiledrop.webserver.service.useruploads

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/test")
class TestController {
    companion object {
        private val logger = LoggerFactory.getLogger(TestController::class.java)
    }

    @GetMapping("/")
    fun something(request: HttpServletRequest): ResponseEntity<Void> {
        logger.info("YOU HIT ME!!")
//        val headers = request.headerNames
//        for (name in headers) {
//            logger.info("$name = ${request.getHeader(name)}")
//        }
        return ResponseEntity.noContent().build()
    }
}