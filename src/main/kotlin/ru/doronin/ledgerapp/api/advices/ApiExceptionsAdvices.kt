package ru.doronin.ledgerapp.api.advices

import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import ru.doronin.ledgerapp.api.exceptions.EmployeeNotFoundException
import ru.doronin.ledgerapp.api.exceptions.FlowOperationNotFoundException

@ControllerAdvice
class EmployeeNotFoundAdvice {

    @ResponseBody
    @ExceptionHandler(EmployeeNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun employeeNotFoundHandler(ex: EmployeeNotFoundException): Map<String, String> =
        mapOf("message" to (ex.message ?: "Employee not found"))
}

@ControllerAdvice
class IllegalStateAdvice {

    @ResponseBody
    @ExceptionHandler(IllegalStateException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun illegalStateHandler(ex: java.lang.IllegalStateException): Map<String, String> =
        mapOf("message" to (ex.message ?: "Internal error"))
}

@ControllerAdvice
class FlowOperationNotFoundAdvice {

    @ResponseBody
    @ExceptionHandler(FlowOperationNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun notFoundHandler(ex: FlowOperationNotFoundException): Map<String, String> =
        mapOf("message" to (ex.message ?: "Operation not found"))
}

@ControllerAdvice
class AccessDeniedAdvice {

    @ResponseBody
    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun accessDeniedHandler(ex: AccessDeniedException): Map<String, String> =
        mapOf("message" to (ex.message ?: "Access denied"))
}