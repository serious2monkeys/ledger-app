package ru.doronin.ledgerapp.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit4.SpringRunner
import ru.doronin.ledgerapp.employee.Employee
import ru.doronin.ledgerapp.employee.EmployeeRepository

@RunWith(SpringRunner::class)
@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Test
    fun `repository properly finds an employee`() {
        //There is no employees in the DB at the very beginning
        assertTrue(employeeRepository.findAll().isEmpty())

        val sampleEmployee = with(Employee()) {
            lastName = "Иванова"
            firstName = "Мария"
            patronymic = "Петровна"
            position = "Бухгалтер"
            this
        }

        employeeRepository.save(sampleEmployee)

        val searchResults = employeeRepository.find("Бухгалтер")
        assertEquals(1, searchResults.size)

        val foundEmployee = searchResults[0]
        with(foundEmployee) {
            assertEquals("Мария", firstName)
            assertEquals("Иванова", lastName)
            assertEquals("Петровна", patronymic)
            assertEquals("Бухгалтер", position)
        }
    }
}