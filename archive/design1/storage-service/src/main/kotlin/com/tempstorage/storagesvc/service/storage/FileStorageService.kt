package com.tempstorage.storagesvc.service.storage

import java.nio.file.Path
import java.util.stream.Stream

interface FileStorageService: StorageService {
    fun deleteAllFilesInFolder()
    fun loadAllFilesFromFolder(): Stream<Path>
}