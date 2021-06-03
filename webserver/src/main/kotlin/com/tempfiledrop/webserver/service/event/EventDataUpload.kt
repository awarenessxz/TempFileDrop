package com.tempfiledrop.webserver.service.event

import com.fasterxml.jackson.annotation.JsonProperty

data class EventDataUpload(
     @JsonProperty("username") val username: String
)