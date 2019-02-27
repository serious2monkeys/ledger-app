package ru.doronin.ledgerapp.employee

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EmployeeRepository : JpaRepository<Employee, Long> {

    fun findByFirstNameAndLastNameAndAndPatronymic(
        first: String,
        last: String,
        patron: String? = null
    ): Optional<Employee>

    @Query(value = "from Employee where concat(firstName,' ', lastName, ' ', patronymic, ' ', position) like '%:pattern%'")
    fun find(pattern: String): List<Employee>
}