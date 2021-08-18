package com.tempstorage.tempfiledrop.webserver.service.useruploads

interface UserUploadInfoService {
    fun addUploadedFilesRecord(record: UserUploadInfo)
    fun getUploadedFilesRecordById(id: String): UserUploadInfo?
    fun getUploadedFilesRecordsForUser(user: String): List<UserUploadInfo>
    fun deleteUploadedFilesRecordById(id: String)
    fun deleteUploadedFilesRecordByObjectName(objectName: String)
}