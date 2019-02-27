package ru.doronin.ledgerapp.bootstrap

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import mu.KotlinLogging
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import ru.doronin.ledgerapp.employee.Employee
import ru.doronin.ledgerapp.employee.EmployeeService
import ru.doronin.ledgerapp.operatons.FlowOperation
import ru.doronin.ledgerapp.operatons.FlowService
import ru.doronin.ledgerapp.operatons.MovementType
import ru.doronin.ledgerapp.user.User
import ru.doronin.ledgerapp.user.UserService
import java.math.BigDecimal
import java.security.SecureRandom
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Stream

/**
 * Компонент, выполняющий начальное заполнение базы данных при запуске системы
 */
@Component
class LedgerBootstrapper(
    val userService: UserService,
    val employeeService: EmployeeService,
    val flowService: FlowService,
    val mapper: ObjectMapper
) : ApplicationListener<ContextRefreshedEvent> {

    private val logger = KotlinLogging.logger("bootstrapper")
    private val random = SecureRandom()

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val configFile = this::class.java.classLoader.getResourceAsStream("config/init.json")
        configFile.use { dataStream ->
            val fileContent = mapper.readTree(dataStream)

            var users: List<User>? = null
            var employees: List<Employee>? = null

            if (fileContent.has("users") && fileContent["users"].isArray) {
                users = readUsers(fileContent["users"] as ArrayNode).map { userService.save(it) }
            }

            if (fileContent.has("employees") && fileContent["employees"].isArray) {
                employees = readEmployees(fileContent["employees"] as ArrayNode).map { employeeService.save(it) }
            }

            if ((users != null) && (employees != null) && (flowService.countRecords() == 0L)) {
                generateFlows(users, employees)
            } else {
                logger.info { "Skipping flows generation" }
            }
        }
    }

    /**
     * Создание тестовых записей о движениях средств
     *
     * @param users List<User> - пользователи
     * @param employees List<Employee> - сотрудники
     */
    fun generateFlows(users: List<User>, employees: List<Employee>) {
        logger.info { "Flows generation started" }

        for (user in users) {
            for (employee in employees) {
                val times: Stream<LocalDateTime> = Stream.iterate(
                    LocalDate.of(2017, 1, 1).atTime(random.nextInt(24), random.nextInt(60))
                ) { t: LocalDateTime -> t.plusMinutes((24 * 60 + (random.nextFloat() * 24 * 30 * 60)).toLong()) }
                times.takeWhile { it.isBefore(LocalDateTime.now()) }
                    .forEach { dateTime ->
                        val operationType: MovementType = when {
                            random.nextInt(3) % 2 == 0 -> MovementType.RECEIPT
                            else -> MovementType.EXPENSE
                        }
                        flowService.save(
                            FlowOperation(
                                type = operationType,
                                description = "Test for ${dateTime.format(
                                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                                )}",
                                date = dateTime,
                                amount = BigDecimal.valueOf(random.nextDouble() * 10000),
                                performer = employee,
                                modifiedBy = user
                            )
                        )
                    }
            }
        }
        logger.info { "Flows generation finished" }
    }

    /**
     * Распознавание набора пользователей в Json-массиве
     *
     * @param usersNode ArrayNode - Json-массив
     */
    private fun readUsers(usersNode: ArrayNode): Set<User> =
        usersNode.asSequence().map { jsonNode -> mapper.treeToValue(jsonNode, User::class.java) }.toSet()

    /**
     * Распознавание набора работников в Json-массиве
     *
     * @param employeeNode ArrayNode - Json-массив
     */
    private fun readEmployees(employeeNode: ArrayNode): Set<Employee> =
        employeeNode.asSequence().map { jsonNode -> mapper.treeToValue(jsonNode, Employee::class.java) }.toSet()
}