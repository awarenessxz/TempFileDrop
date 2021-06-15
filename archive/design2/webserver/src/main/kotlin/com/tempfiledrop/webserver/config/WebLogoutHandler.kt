//package com.tempfiledrop.webserver.config
//
//import org.keycloak.adapters.AdapterDeploymentContext
//import org.keycloak.adapters.KeycloakDeployment
//import org.keycloak.adapters.RefreshableKeycloakSecurityContext
//import org.keycloak.adapters.spi.HttpFacade
//import org.keycloak.adapters.springsecurity.facade.SimpleHttpFacade
//import org.keycloak.adapters.springsecurity.token.AdapterTokenStoreFactory
//import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
//import org.keycloak.adapters.springsecurity.token.SpringSecurityAdapterTokenStoreFactory
//import org.slf4j.LoggerFactory
//import org.springframework.security.core.Authentication
//import org.springframework.security.web.authentication.logout.LogoutHandler
//import org.springframework.stereotype.Service
//import javax.servlet.http.HttpServletRequest
//import javax.servlet.http.HttpServletResponse
//
//@Service
//class WebLogoutHandler(
//        private val adapterDeploymentContext: AdapterDeploymentContext,
//): LogoutHandler {
//    companion object {
//        private val logger = LoggerFactory.getLogger(WebLogoutHandler::class.java)
//        private val adapterTokenStoreFactory: AdapterTokenStoreFactory = SpringSecurityAdapterTokenStoreFactory()
//    }
//
//    override fun logout(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
//        if (authentication == null) {
//            logger.warn("Cannot logout without authentication")
//            return
//        } else if (authentication !is KeycloakAuthenticationToken) {
//            logger.warn("Cannot logout non-keycloak authentication")
//            return
//        }
//        handleSingleSignOut(request, response, authentication)
//    }
//
//    protected fun handleSingleSignOut(request: HttpServletRequest?, response: HttpServletResponse?, authenticationToken: KeycloakAuthenticationToken) {
//        val facade: HttpFacade = SimpleHttpFacade(request, response)
//        val deployment: KeycloakDeployment = adapterDeploymentContext.resolveDeployment(facade)
//        adapterTokenStoreFactory.createAdapterTokenStore(deployment, request, response).logout()
//        val session = authenticationToken.account.keycloakSecurityContext as RefreshableKeycloakSecurityContext
//        session.logout(deployment)
//    }
//}