package ru.doronin.ledgerapp.api.controllers

import org.springframework.beans.BeanUtils
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.*
import ru.doronin.ledgerapp.api.dtos.FlowDto
import ru.doronin.ledgerapp.api.dtos.FlowsPage
import ru.doronin.ledgerapp.api.exceptions.EmployeeNotFoundException
import ru.doronin.ledgerapp.api.exceptions.FlowOperationNotFoundException
import ru.doronin.ledgerapp.config.toDto
import ru.doronin.ledgerapp.employee.EmployeeService
import ru.doronin.ledgerapp.operatons.FlowOperation
import ru.doronin.ledgerapp.operatons.FlowService
import ru.doronin.ledgerapp.operatons.MonthlyOperationsReport
import ru.doronin.ledgerapp.user.UserRole
import ru.doronin.ledgerapp.user.UserService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Контроллер обращений методам REST-API, относящимся к данным о движениях средств
 */
@RestController
@RequestMapping(path = ["flows"], produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
class FlowController(
    private val flowService: FlowService,
    private val userService: UserService,
    private val employeeService: EmployeeService
) {

    /**
     * Загрузка полных данных по выбранной операции
     * @param id - идентификатор операции
     * @return FlowOperation
     */
    @GetMapping(path = ["{id}"])
    fun load(@PathVariable("id") id: Long): FlowOperation =
        flowService.findById(id).orElseThrow { FlowOperationNotFoundException(id) }

    /**
     * Загрузка всех доступных данных о движениях средств с постраничной выдачей результатов
     *
     * @param pageParam Int? - номер страницы (по умолчанию 1)
     * @param sizeParam Int? - размер страницы (по умолчанию 20)
     */
    @GetMapping(path = ["all"])
    fun loadAll(
        @RequestParam("page") pageParam: Int?,
        @RequestParam("size") sizeParam: Int?,
        @RequestParam("modifier") modifierLogin: String?,
        @RequestParam("performer") performerId: Long?
    ): FlowsPage {
        val currentUser = userService.getCurrent()

        val page = (pageParam ?: 1) - 1
        val size = sizeParam ?: 20
        val performer = performerId?.let { employeeService.load(it).orElse(null) }

        val flows =
            flowService.findByCriteria(
                page = page,
                pageSize = size,
                performer = performer,
                modifier = if (UserRole.ADMIN == currentUser.role) {
                    modifierLogin?.let { userService.findByLogin(it).orElse(null) }
                } else currentUser
            )

        return FlowsPage(
            pageNum = page + 1,
            pageSize = size,
            totalPages = flows.totalPages,
            totalElements = flows.totalElements,
            flows = flows.content.map(FlowOperation::toDto)
        )
    }

    /**
     * Изменение записи о движении средств
     *
     * @param storedFlow FlowOperation - хранящаяся версия
     * @param flow FlowDto - версия с изменениями
     */
    @PutMapping(path = ["{id}"])
    fun update(
        @PathVariable("id") storedFlow: FlowOperation,
        @RequestBody flow: FlowDto
    ): FlowDto {
        BeanUtils.copyProperties(flow, storedFlow, "id")

        val currentUser = userService.getCurrent()
        storedFlow.modifiedBy = currentUser

        val employee =
            employeeService.load(flow.performerId).orElseThrow { EmployeeNotFoundException(flow.performerId) }
        storedFlow.performer = employee

        return flowService.save(storedFlow).toDto()
    }

    /**
     * Добавление новой записи о движении средств
     *
     * @param flow FlowDto - данные записи
     */
    @PostMapping
    fun create(@RequestBody flow: FlowDto): FlowDto {
        val currentUser = userService.getCurrent()

        val performer =
            employeeService.load(flow.performerId).orElseThrow { EmployeeNotFoundException(flow.performerId) }

        val operation = FlowOperation(modifiedBy = currentUser, performer = performer)
        BeanUtils.copyProperties(flow, operation)

        return flowService.save(operation).toDto()
    }

    /**
     * Удаление записи о движении средств
     *
     * @param storedFlow FlowOperation - запись
     */
    @DeleteMapping(path = ["{id}"])
    fun deleteFlow(@PathVariable("id") storedFlow: FlowOperation): Map<String, String> {
        val currentUser = userService.getCurrent()
        if ((UserRole.ADMIN != currentUser.role) && (currentUser.id != storedFlow.modifiedBy.id)) {
            throw AccessDeniedException("You have no authorities to delete the operation data")
        }
        flowService.delete(storedFlow)
        return mapOf("message" to "Done")
    }


    /**
     * Запрос отчёта о движениях средств
     *
     * @param begin = начало периода
     * @param end - конец периада
     */
    @GetMapping(path = ["report"])
    fun generateReport(
        @RequestParam("begin") begin: String?,
        @RequestParam("end") end: String?
    ): MonthlyOperationsReport {
        if (UserRole.ADMIN != userService.getCurrent().role) {
            throw AccessDeniedException("Reports designed only for admins")
        }

        val beginDateTime: LocalDateTime? =
            begin?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay() }

        val endDateTime: LocalDateTime? =
            end?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE).atTime(LocalTime.MAX) }

        return MonthlyOperationsReport(flowService.findAll(beginDateTime, endDateTime))
    }
}