package com.tempfiledrop.webserver.service.userinfo

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user-info")
class UserInfoController(
        private val service: UserInfoService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserInfoController::class.java)
    }

    @GetMapping("/exists/{username}")
    fun checkUserExists(@PathVariable("username") username: String): ResponseEntity<Boolean> {
        val user = service.getUserInfoByUsername(username)
        return ResponseEntity(user != null, HttpStatus.OK)
    }

    @PostMapping("/login")
    fun userLogin(@RequestBody userInfoReq: UserInfoRequest): ResponseEntity<Boolean> {
        val user = service.attemptToLogin(userInfoReq.username, userInfoReq.password)
        return ResponseEntity(user != null, HttpStatus.OK)
    }
}