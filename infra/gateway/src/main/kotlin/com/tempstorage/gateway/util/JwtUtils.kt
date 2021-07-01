package com.tempstorage.gateway.util

import org.slf4j.LoggerFactory
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.util.StringUtils

object JwtUtils {
    private const val AUTHORIZATION_HEADER = "Authorization"
    private const val BEARER_PREFIX = "Bearer "
    private val logger = LoggerFactory.getLogger(JwtUtils::class.java)

    fun extractTokenFromRequestHeader(request: ServerHttpRequest): String? {
        val bearerToken = request.headers.getOrEmpty(AUTHORIZATION_HEADER)[0]
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null
    }

//    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
//    private fun generatePublicKey(publicKey: String): PublicKey {
//        val kf = KeyFactory.getInstance("RSA")
//        val pubKeySpecX509EncodedKeySpec = X509EncodedKeySpec(Base64.getDecoder().decode(publicKey))
//        return kf.generatePublic(pubKeySpecX509EncodedKeySpec)
//    }
//
//    @Throws(JwtException::class)
//    fun getAuthentication(token: String, key: String, client: String, useClientRole: Boolean): Authentication {
//        val publicKey = generatePublicKey(key)
//        val claims = Jwts.parser()
//                .setSigningKey(publicKey)
//                .parseClaimsJws(token)
//                .body
//        logger.debug("Token Claims: $claims")
//
//        // extract roles from token
//        val objectMapper = ObjectMapper().registerKotlinModule()
//        val accessToken = objectMapper.convertValue(claims, AccessToken::class.java)
//        val authorities = if (useClientRole) {
//            accessToken.getResourceAccess(client).roles.map { SimpleGrantedAuthority(it) }
//        } else {
//            accessToken.realmAccess.roles.map { SimpleGrantedAuthority(it) }
//        }
//        // extract custom storage attributes
//        val storageAttrs = objectMapper.convertValue(claims["storage_client_attr"], StorageAttributes::class.java)
//
//        // Use SimpleAuthorityMapper to add prefix ROLE_ to all keycloak roles
//        val grantedAuthorities = SimpleAuthorityMapper().mapAuthorities(authorities)
//        logger.debug("Token Roles: $grantedAuthorities")
//
//        val principal = JwtUser(claims.subject, grantedAuthorities, storageAttrs)
//        return UsernamePasswordAuthenticationToken(principal, token, grantedAuthorities)
//    }
}