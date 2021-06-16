package com.tempfiledrop.storagesvc.service.download

interface DownloadTokenService {
    fun generateDownloadToken(storageId: String): DownloadToken
    fun checkIfTokenExpired(downloadKey: String): DownloadToken?
}