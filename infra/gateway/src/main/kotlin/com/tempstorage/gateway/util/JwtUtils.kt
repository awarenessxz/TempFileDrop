package com.tempstorage.gateway.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.tempstorage.gateway.model.JwtUser
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.keycloak.representations.AccessToken
import org.slf4j.LoggerFactory
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.util.StringUtils
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.util.*

object JwtUtils {
    private const val AUTHORIZATION_HEADER = "Authorization"
    private const val BEARER_PREFIX = "Bearer "
    private val logger = LoggerFactory.getLogger(JwtUtils::class.java)

    fun extractBearerToken(request: ServerHttpRequest): String? {
        val bearerTokenHeader = request.headers.getOrEmpty(AUTHORIZATION_HEADER)
        if (bearerTokenHeader.isNotEmpty()) {
            val bearerToken = bearerTokenHeader[0]
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
                return bearerToken.substring(7);
            }
        }
        return null
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    private fun generatePublicKey(publicKey: String): PublicKey {
        val kf = KeyFactory.getInstance("RSA")
        val pubKeySpecX509EncodedKeySpec = X509EncodedKeySpec(Base64.getDecoder().decode(publicKey))
        return kf.generatePublic(pubKeySpecX509EncodedKeySpec)
    }

    @Throws(JwtException::class)
    fun getJwtUser(token: String, key: String, client: String, useClientRole: Boolean): JwtUser {
        val publicKey = generatePublicKey(key)
        val claims = Jwts.parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(token)
                .body
        logger.debug("Token Claims: $claims")

        val objectMapper = ObjectMapper().registerKotlinModule()
        val accessToken = objectMapper.convertValue(claims, AccessToken::class.java)
        val roles = if (useClientRole) {
            accessToken.getResourceAccess(client).roles
        } else {
            accessToken.realmAccess.roles
        }
        return JwtUser(accessToken.name, accessToken.preferredUsername, roles.toList())
    }
}