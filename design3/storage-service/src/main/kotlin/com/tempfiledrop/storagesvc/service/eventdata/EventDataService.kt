package com.tempfiledrop.storagesvc.service.eventdata

import com.tempfiledrop.storagesvc.service.event.EventMessage

interface EventDataService {
    fun writeToDB(eventMsg: EventMessage)
}