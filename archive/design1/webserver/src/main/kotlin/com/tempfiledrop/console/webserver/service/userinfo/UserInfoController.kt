package com.tempfiledrop.console.webserver.service.userinfo

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user-info")
class UserInfoController(
        private val service: UserInfoService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserInfoController::class.java)
    }

    @GetMapping("/exists/{username}")
    fun checkUserExists(@PathVariable("username") username: String): ResponseEntity<UserInfoResponse> {
        val user = service.getUserInfoByUsername(username)
        val userExists = user != null
        val response = UserInfoResponse(userExists, username)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @PostMapping("/login")
    fun userLogin(@RequestBody userInfoReq: UserInfoRequest): ResponseEntity<UserInfoResponse> {
        val user = service.attemptToLogin(userInfoReq.username, userInfoReq.password)
        val userExists = user != null
        val response = UserInfoResponse(userExists, userInfoReq.username)
        return ResponseEntity(response, HttpStatus.OK)
    }
}