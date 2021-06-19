package com.tempfiledrop.webserver.util

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.keycloak.representations.AccessToken
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper
import org.springframework.security.core.userdetails.User
import org.springframework.util.StringUtils
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.servlet.http.HttpServletRequest

object JwtUtils {
    private const val AUTHORIZATION_HEADER = "Authorization"
    private const val BEARER_PREFIX = "Bearer "
    private val logger = LoggerFactory.getLogger(JwtUtils::class.java)

    fun extractTokenFromRequestHeader(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
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
    fun getAuthentication(token: String, key: String, client: String, useClientRole: Boolean): Authentication {
        val publicKey = generatePublicKey(key)
        val claims = Jwts.parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(token)
                .body
        logger.debug("Token Claims: $claims")

        // extract roles from token
        val accessToken = ObjectMapper().convertValue(claims, AccessToken::class.java)
        val authorities = if (useClientRole) {
            accessToken.getResourceAccess(client).roles.map { SimpleGrantedAuthority(it) }
        } else {
            accessToken.realmAccess.roles.map { SimpleGrantedAuthority(it) }
        }

        // Use SimpleAuthorityMapper to add prefix ROLE_ to all keycloak roles
        val grantedAuthorities = SimpleAuthorityMapper().mapAuthorities(authorities)
        logger.debug("Token Roles: $grantedAuthorities")

        val principal = User(claims.subject, "", grantedAuthorities)
        return UsernamePasswordAuthenticationToken(principal, token, grantedAuthorities)
    }
}