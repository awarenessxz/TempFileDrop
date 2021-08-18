package com.tempstorage.tempfiledrop.webserver.service.useruploads

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="users_upload_info")
data class UserUploadInfo (
    val user: String,                       // user
    val objectName: String,                 // objectName
    @Id var id: ObjectId = ObjectId.get()   // mongoDB ID
)