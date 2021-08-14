package com.tempstorage.storagesvc.service.eventdata

import com.tempstorage.storagesvc.service.notification.NotificationMessage

interface EventDataService {
    fun writeToDB(eventMsg: NotificationMessage)
    fun getAllEventsFromDB(): List<EventData>
    fun getEventsByBucket(bucket: String): List<EventData>
}