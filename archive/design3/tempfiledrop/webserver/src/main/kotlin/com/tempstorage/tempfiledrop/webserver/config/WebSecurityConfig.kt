package com.tempstorage.tempfiledrop.webserver.config

import com.tempstorage.tempfiledrop.webserver.security.JwtAccessDeniedHandler
import com.tempstorage.tempfiledrop.webserver.security.JwtAuthenticationEntryPoint
import com.tempstorage.tempfiledrop.webserver.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
        private val jwtAccessDeniedHandler: JwtAccessDeniedHandler,
        private val jwtAuthenticationErrorHandler: JwtAuthenticationEntryPoint,
        private val jwtAuthenticationFilter: JwtAuthenticationFilter
): WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http.csrf().disable()   // do not need this as tokens are immune to csrf
        http.exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationErrorHandler)
                .accessDeniedHandler(jwtAccessDeniedHandler)
        http.headers().frameOptions().sameOrigin()
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // do not create sessions since we are using tokens for each request
        http.authorizeRequests()
                .antMatchers("/**").hasAnyRole("tempfiledrop|user", "tempfiledrop|admin")
                .anyRequest().denyAll()
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
    }
}