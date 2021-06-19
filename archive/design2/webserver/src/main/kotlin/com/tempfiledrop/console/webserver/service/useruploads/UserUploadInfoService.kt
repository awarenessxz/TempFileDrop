package com.tempfiledrop.console.webserver.service.useruploads

interface UserUploadInfoService {
    fun addUploadedFilesRecord(record: UserUploadInfo)
    fun getUploadedFilesRecordById(id: String): UserUploadInfo?
    fun getUploadedFilesRecordByStorageId(storageId: String): UserUploadInfo?
    fun getUploadedFilesRecordsForUser(user: String): List<UserUploadInfo>
    fun deleteUploadedFilesRecordById(id: String)
    fun deleteUploadedFilesRecordByStorageId(storageId: String)
}