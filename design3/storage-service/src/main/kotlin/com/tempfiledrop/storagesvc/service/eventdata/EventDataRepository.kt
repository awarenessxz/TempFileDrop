package com.tempfiledrop.storagesvc.service.eventdata

import org.springframework.data.mongodb.repository.MongoRepository

interface EventDataRepository: MongoRepository<EventData, String> {

}