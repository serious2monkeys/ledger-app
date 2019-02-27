package ru.doronin.ledgerapp.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Настройка перенаправления с начальной страницы на страницу документации по REST-API
 */
@Configuration
class StartPageConfig : WebMvcConfigurer {

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addRedirectViewController("/", "/swagger-ui.html")
    }
}