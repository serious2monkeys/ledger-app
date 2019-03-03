package ru.doronin.ledgerapp.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import io.restassured.http.ContentType
import io.restassured.module.mockmvc.RestAssuredMockMvc
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import ru.doronin.ledgerapp.api.controllers.FlowController
import ru.doronin.ledgerapp.api.dtos.MonthlyOperationsReport
import ru.doronin.ledgerapp.api.dtos.StatisticsElement
import ru.doronin.ledgerapp.employee.EmployeeService
import ru.doronin.ledgerapp.operatons.FlowService
import ru.doronin.ledgerapp.user.UserService
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

@RunWith(MockitoJUnitRunner::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FlowOperationsApiTest {

    private lateinit var mvc: MockMvc

    private val apiEndpoint = "/flows/report?begin=2018-01-01&end=2018-02-28"

    @Mock
    private lateinit var flowService: FlowService

    @Mock
    private lateinit var userService: UserService

    @Mock
    private lateinit var employeeService: EmployeeService

    private lateinit var sampleReport: MonthlyOperationsReport

    @Before
    fun initMocks() {
        mvc = MockMvcBuilders.standaloneSetup(FlowController(flowService, userService, employeeService))
            .build()

        RestAssuredMockMvc.mockMvc(mvc)

        val january = LocalDate.of(2018, 1, 1)
        val february = LocalDate.of(2018, 2, 1)

        val sampleStatistics = mapOf(
            january to StatisticsElement(
                operations = 2,
                receipt = BigDecimal("1370.00"),
                expense = BigDecimal("6300.00")
            ),
            february to StatisticsElement(
                operations = 3,
                receipt = BigDecimal("37000.00"),
                expense = BigDecimal("7400.00")
            )
        )
        sampleReport = MonthlyOperationsReport()
        sampleReport.statistics.putAll(sampleStatistics)

        Mockito.doReturn(sampleReport)
            .`when`(flowService).createReport(
                eq(january.atStartOfDay()),
                eq(february.withDayOfMonth(28).atTime(LocalTime.MAX))
            )
    }

    @Test
    fun testController() {

        val response = RestAssuredMockMvc.given().`when`().get(apiEndpoint)
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .contentType(ContentType.JSON)
            .extract().response().`as`(ObjectNode::class.java)

        assertNotNull(response)

        val mapper = ObjectMapper()
        mapper.nodeFactory = JsonNodeFactory.withExactBigDecimals(true)

        val sampleJson = mapper.valueToTree<ObjectNode>(sampleReport)
        testOutput(sampleJson, response)
    }

    /**
     * Сравнение полученного Json с ожидаемым
     *
     * @param expectedNode  - ожидаемый объект
     * @param actualNode    - полученный объект
     */
    private fun testOutput(expectedNode: ObjectNode, actualNode: ObjectNode) {
        assertEquals(expectedNode.size(), actualNode.size())
        assertTrue(expectedNode.fieldNames().asSequence().all { actualNode.has(it) })
        assertTrue(actualNode.all { node -> node.isObject })

        expectedNode.fields().forEach { field ->
            val expectedElement = field.value as ObjectNode
            assertTrue(actualNode.has(field.key))

            val actualElement = actualNode[field.key] as ObjectNode
            assertTrue((actualElement.size() == expectedElement.size())
                    && actualElement.fieldNames().asSequence().all { expectedElement.has(it) })

            expectedElement.fields().forEach { statisticsField ->
                when {
                    statisticsField.value.isBigDecimal -> assertEquals(
                        statisticsField.value.asDouble(),
                        actualElement[statisticsField.key].asDouble(),
                        0.001
                    )
                    else -> assertEquals(statisticsField.value, actualElement[statisticsField.key])
                }
            }
        }
    }
}