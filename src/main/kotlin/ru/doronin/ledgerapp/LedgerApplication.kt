package ru.doronin.ledgerapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@SpringBootApplication
@EnableJpaRepositories
@EnableSwagger2
class LedgerApplication {

    /**
     * Компонент для формирования документации по API
     */
    @Bean
    fun api(): Docket = Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage("ru.doronin.ledgerapp.api.controllers"))
        .paths(PathSelectors.any())
        .build()
        .apiInfo(generateInfo())

    fun generateInfo(): ApiInfo = ApiInfo(
        "Rest API for ledger application",
        "The API was designed as a part of test application",
        "0.5.SNAPSHOT",
        "Keep calm and use at your own risk.",
        Contact("Anton Doronin", "https://github.com/serious2monkeys", "doronantonin@gmail.com"),
        null, null, emptyList()
    )
}

fun main(args: Array<String>) {
    runApplication<LedgerApplication>(*args)
}
