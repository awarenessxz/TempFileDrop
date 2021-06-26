package com.tempstorage.storagesvc.service.download

interface DownloadTokenService {
    fun generateDownloadToken(storageId: String): DownloadToken
    fun checkIfTokenExpired(downloadKey: String): DownloadToken?
}