package com.tempstorage.storagesvc.service.eventdata

import com.tempstorage.storagesvc.service.event.EventMessage

interface EventDataService {
    fun writeToDB(eventMsg: EventMessage)
    fun getAllEventsFromDB(): List<EventData>
    fun getEventsByBucket(bucket: String): List<EventData>
}