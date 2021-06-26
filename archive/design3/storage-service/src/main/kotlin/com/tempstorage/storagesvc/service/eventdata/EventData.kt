package com.tempstorage.storagesvc.service.eventdata

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime

@Document(collection="data_events")
data class EventData(
        val bucket: String,                        // Bucket
        val storageId: String,                     // Storage Id
        val storageFiles: String,                  // files inside storage id
        val eventType: String,                     // event Type (upload / download / delete)
        val publishedDateTime: ZonedDateTime,      // datetime event was published
        @Id val id: String? = null                 // mongoDB ID
)
