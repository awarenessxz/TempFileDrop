package com.tempstorage.storagesvc.controller.monitor

import com.tempstorage.storagesvc.service.eventdata.EventData
import com.tempstorage.storagesvc.service.eventdata.EventDataService
import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/storagesvc/events")
class EventsController(
        private val eventDataService: EventDataService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(EventsController::class.java)
    }

    @Operation(summary = "Get all events that were published to message queue")
    @GetMapping
    fun getAllEvents(): ResponseEntity<List<EventData>> {
        logger.info("Receiving Request to retrieve all notifications published")
        val events = eventDataService.getAllEventsFromDB()
        return ResponseEntity(events, HttpStatus.OK)
    }

    @Operation(summary = "Get all events that were published to message queue for bucket")
    @GetMapping("/{bucket}")
    fun getAllEvents(@PathVariable("bucket") bucket: String): ResponseEntity<List<EventData>> {
        logger.info("Receiving Request to retrieve all notifications published for Bucket=$bucket")
        val events = eventDataService.getEventsByBucket(bucket)
        return ResponseEntity(events, HttpStatus.OK)
    }
}