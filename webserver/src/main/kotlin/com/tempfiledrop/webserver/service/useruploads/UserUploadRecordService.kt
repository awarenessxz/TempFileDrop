package com.tempfiledrop.webserver.service.useruploads

interface UserUploadRecordService {
    fun addUserUploadRecord(record: UserUploadRecord)
    fun getUserUploadRecordById(id: String): UserUploadRecord?
    fun listUserUploadRecord(folderName: String): List<UserUploadRecord>
    fun deleteUserUploadRecord(id: String)
}