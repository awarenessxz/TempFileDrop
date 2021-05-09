package com.tempfiledrop.webserver.service.uploadedfiles

import com.tempfiledrop.webserver.exception.ApiException
import com.tempfiledrop.webserver.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class UploadedFilesServiceImpl(
        private val repository: UploadedFilesRepository
): UploadedFilesService {
    override fun addUploadedFilesRecord(record: UploadedFiles) {
        repository.save(record)
    }

    override fun getUploadedFilesRecordById(id: String): UploadedFiles? {
        return repository.findByIdOrNull(id)
    }

    override fun getUploadedFilesRecordByStorageId(storageId: String): UploadedFiles? {
        return repository.findByStorageId(storageId)
    }

    override fun getUploadedFilesRecordsForUser(user: String): List<UploadedFiles> {
        return repository.findByUser(user)
    }

    override fun deleteUploadedFilesRecordById(id: String) {
        repository.deleteById(id)
    }

    override fun reduceDownloadCountOfUploadedFiles(storageId: String) {
        val record = repository.findByStorageId((storageId)) ?: throw ApiException("Record not found!", ErrorCode.NO_MATCHING_RECORD, HttpStatus.BAD_REQUEST)
        val newRecord = UploadedFiles(
                record.id,
                record.user,
                record.filenames,
                record.numOfDownloadsLeft - 1,
                record.expiryDatetime,
                record.storageId,
                record.downloadLink
        )
        repository.save(newRecord)
    }
}