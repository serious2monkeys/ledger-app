package ru.doronin.ledgerapp.api.controllers

import org.springframework.beans.BeanUtils
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import ru.doronin.ledgerapp.api.exceptions.EmployeeNotFoundException
import ru.doronin.ledgerapp.employee.Employee
import ru.doronin.ledgerapp.employee.EmployeeService

/**
 * Контроллер обращений к методам API, относящимся данным сотрудников
 */
@RestController
@RequestMapping(path = ["employee"], produces = [MediaType.APPLICATION_JSON_VALUE])
class EmployeesController(private val employeeService: EmployeeService) {

    /**
     * Перечисление сотрудников
     *
     */
    @GetMapping(path = ["all"])
    fun listEmployees(): List<Employee> = employeeService.loadAll()

    /**
     * Загрузка данных сотрудников
     *
     * @param id  id: Long - идентификатор сотрудника
     */
    @GetMapping(path = ["{id}"])
    fun getEmployee(@PathVariable(value = "id") id: Long): Employee =
        employeeService.load(id).orElseThrow { EmployeeNotFoundException(id) }

    /**
     * Изменение данных о сотруднике
     *
     * @param employeeFromDb Employee - исходный объект
     * @param sendEmployee Employee - изменения
     */
    @PutMapping(path = ["{id}"])
    fun updateEmployee(@PathVariable("id") employeeFromDb: Employee, sendEmployee: Employee): Employee {
        BeanUtils.copyProperties(sendEmployee, employeeFromDb, "id")
        return employeeService.save(employeeFromDb)
    }

    /**
     * Добавление сведений о сотруднике
     *+
     * @param employee Employee - новые данные
     */
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun createEmployee(@RequestBody employee: Employee): Employee = employeeService.save(employee)
}