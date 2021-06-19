package com.tempstorage.storagesvc.service.download

import org.springframework.data.mongodb.repository.MongoRepository

interface DownloadTokenRepository: MongoRepository<DownloadToken, String> {
    fun findByDownloadKey(downloadKey: String): DownloadToken?
}