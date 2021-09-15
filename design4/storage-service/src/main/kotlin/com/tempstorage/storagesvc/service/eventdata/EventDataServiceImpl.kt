package com.tempstorage.storagesvc.service.eventdata

import com.tempstorage.storagesvc.service.notification.NotificationMessage
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class EventDataServiceImpl(
        private val repository: EventDataRepository
): EventDataService {
    override fun writeToDB(eventMsg: NotificationMessage) {
        val eventData = EventData(eventMsg.bucket, eventMsg.objectName, eventMsg.eventType.name, ZonedDateTime.now())
        repository.save(eventData)
    }

    override fun getAllEventsFromDB(): List<EventData> {
        return repository.findAll()
    }

    override fun getEventsByBucket(bucket: String): List<EventData> {
        return repository.findByBucket(bucket)
    }
}