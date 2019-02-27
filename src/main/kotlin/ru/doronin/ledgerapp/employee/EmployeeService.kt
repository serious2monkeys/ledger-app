package ru.doronin.ledgerapp.employee

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Служба работы с данными сотрудников
 */
@Service
class EmployeeService(private val employeeRepository: EmployeeRepository) {

    /**
     * Загрузка данных всех сотрудников
     */
    fun loadAll(): List<Employee> = employeeRepository.findAll()

    /**
     * Поиск работника по id
     *
     * @param id Long
     */
    fun load(id: Long) = employeeRepository.findById(id)

    /**
     * Поиск сотрудников
     */
    fun find(pattern: String) = employeeRepository.find(pattern)

    /**
     * Сохранение данных сотрудника
     */
    @Transactional
    fun save(employee: Employee): Employee {
        employee.id = employee.id ?: employeeRepository.findByFirstNameAndLastNameAndAndPatronymic(
            employee.firstName,
            employee.lastName,
            employee.patronymic
        ).orElse(employee).id
        return employeeRepository.save(employee)
    }
}