package com.tempfiledrop.webserver.service.storagesvcclient

import com.fasterxml.jackson.annotation.JsonProperty

data class StorageResponse(
        @JsonProperty val message: String,
        @JsonProperty val storageId: String
)