package com.tempfiledrop.storagesvc.service.download

import com.tempfiledrop.storagesvc.exception.ApiException
import com.tempfiledrop.storagesvc.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

@Service
class DownloadTokenServiceImpl(
        private val downloadTokenRepository: DownloadTokenRepository
): DownloadTokenService {
    companion object {
        private val logger = LoggerFactory.getLogger(DownloadTokenServiceImpl::class.java)
    }

    override fun generateDownloadToken(storageId: String): DownloadToken {
        val expiryDatetime = ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(15)
        val downloadKey = UUID.randomUUID()
        val token = DownloadToken(expiryDatetime, downloadKey.toString(), storageId)
        downloadTokenRepository.save(token)
        return token
    }

    override fun checkIfTokenExpired(downloadKey: String): DownloadToken? {
        val token = downloadTokenRepository.findByDownloadKey(downloadKey)
        if (token === null) {
            throw ApiException("Token is lost", ErrorCode.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR)
        } else {
            logger.info("TOKEN == $token")
            if (token.expiryDatetime.toInstant().isAfter(ZonedDateTime.now().toInstant())) {
                return token
            } else {
                downloadTokenRepository.deleteById(token.id.toString())
                throw ApiException("Download link expired", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
            }
        }
    }
}