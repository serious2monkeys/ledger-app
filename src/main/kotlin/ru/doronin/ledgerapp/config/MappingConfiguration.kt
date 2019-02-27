package ru.doronin.ledgerapp.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.doronin.ledgerapp.api.dtos.FlowDto
import ru.doronin.ledgerapp.employee.EmployeeService
import ru.doronin.ledgerapp.operatons.FlowOperation
import ru.doronin.ledgerapp.user.UserService
import springfox.documentation.swagger2.configuration.Swagger2JacksonModule
import kotlin.reflect.full.memberProperties

@Configuration
class MappingConfiguration {

    /**
     * Настройка Jackson-преобразователя в/из JSON
     */
    @Bean
    fun mapper(): ObjectMapper {
        val mapper = ObjectMapper().registerKotlinModule()
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true)

        mapper.registerModule(JavaTimeModule())
        mapper.registerModule(Swagger2JacksonModule())

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        val filterProvider = SimpleFilterProvider()
        filterProvider.addFilter(
            "ignoringPassword",
            SimpleBeanPropertyFilter.serializeAllExcept("password")
        )
        mapper.setFilterProvider(filterProvider)

        return mapper
    }
}

/**
 * Преобразуем FlowOperation во FlowDto с помощью рефлексии
 */
fun FlowOperation.toDto(): FlowDto = with(::FlowDto) {
    val propertiesByName = FlowOperation::class.memberProperties.associateBy { it.name }
    callBy(parameters.associate { parameter ->
        parameter to when (parameter.name) {
            FlowDto::modifierId.name -> modifiedBy.id
            FlowDto::performerId.name -> performer.id
            else -> propertiesByName[parameter.name]?.get(this@toDto)
        }
    })
}