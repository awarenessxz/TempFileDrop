package com.tempfiledrop.webserver.service.event

import com.fasterxml.jackson.annotation.JsonProperty

data class EventDataDelete(
     @JsonProperty("recordId") val recordId: String
)