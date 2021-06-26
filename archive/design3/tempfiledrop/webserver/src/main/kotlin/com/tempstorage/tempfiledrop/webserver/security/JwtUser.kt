package com.tempstorage.tempfiledrop.webserver.security

import org.springframework.security.core.GrantedAuthority

data class JwtUser(
        val username: String,
        val authorities: Set<GrantedAuthority>,
        val storageAttrs: StorageAttributes
)