package com.tempstorage.storagesvc.security

import org.springframework.security.core.GrantedAuthority

data class JwtUser(
        val username: String,
        val authorities: Set<GrantedAuthority>,
        val storageAttrs: StorageAttributes
)