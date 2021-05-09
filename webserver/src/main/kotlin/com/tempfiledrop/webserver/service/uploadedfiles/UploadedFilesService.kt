package com.tempfiledrop.webserver.service.uploadedfiles

interface UploadedFilesService {
    fun addUploadedFilesRecord(record: UploadedFiles)
    fun getUploadedFilesRecordById(id: String): UploadedFiles?
    fun getUploadedFilesRecordByStorageId(storageId: String): UploadedFiles?
    fun getUploadedFilesRecordsForUser(user: String): List<UploadedFiles>
    fun deleteUploadedFilesRecordById(id: String)
    fun reduceDownloadCountOfUploadedFiles(storageId: String)
}