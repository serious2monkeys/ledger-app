package ru.doronin.ledgerapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler
import ru.doronin.ledgerapp.security.CustomUserDetailsService

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@Configuration
class SecurityConfiguration(val userDetailsService: CustomUserDetailsService, val encoder: PasswordEncoder) :
    WebSecurityConfigurerAdapter() {

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService)
        authProvider.setPasswordEncoder(encoder)
        return authProvider
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(authenticationProvider())
    }

    @Bean
    fun hierarchy(): RoleHierarchy {
        val hierarchy = RoleHierarchyImpl()
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_OPERATOR")
        return hierarchy
    }

    override fun configure(web: WebSecurity?) {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)
        val handler = DefaultWebSecurityExpressionHandler()
        handler.setRoleHierarchy(hierarchy())
        handler.setApplicationContext(applicationContext)
        http.authorizeRequests()
            .expressionHandler(handler)
            .antMatchers("/flows/report/**").hasRole("ADMIN")
            .anyRequest().fullyAuthenticated()
            .and()
            .httpBasic()
    }

}