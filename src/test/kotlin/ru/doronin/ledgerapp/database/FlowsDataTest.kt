package ru.doronin.ledgerapp.database

import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.spring.api.DBRider
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import ru.doronin.ledgerapp.api.dtos.StatisticsElement
import ru.doronin.ledgerapp.operatons.FlowRepository
import ru.doronin.ledgerapp.operatons.FlowService
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@RunWith(SpringRunner::class)
@DataJpaTest
@DataSet(
    value = ["report_set.yml"],
    cleanAfter = true,
    cleanBefore = true,
    disableConstraints = true,
    executorId = "reports"
)
@DBRider
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class FlowsDataTest {

    @TestConfiguration
    class Configuration {
        @Autowired
        private lateinit var repository: FlowRepository

        @Bean
        fun service(): FlowService = FlowService(repository)
    }

    @Autowired
    private lateinit var flowService: FlowService

    @Autowired
    private lateinit var flowRepository: FlowRepository

    @Test
    fun `repository properly finds flows by period`() {
        var startCriterion = LocalDate.of(2018, 1, 1).atStartOfDay()
        var endCriterion = LocalDate.of(2018, 2, 1).atStartOfDay()

        testSearchResults(startCriterion, endCriterion, 3)

        startCriterion = LocalDate.of(2018, 2, 1).atStartOfDay()
        endCriterion = LocalDate.of(2018, 2, 28).atTime(LocalTime.MAX)

        testSearchResults(startCriterion, endCriterion, 2)
    }

    fun testSearchResults(periodBegin: LocalDateTime, periodEnd: LocalDateTime, expectedCount: Int) {
        val searchResult = flowRepository.findAllByDateBetweenOrderByDateDesc(periodBegin, periodEnd)

        assertEquals(expectedCount, searchResult.size)

        Assert.assertTrue(searchResult.all { operation ->
            !operation.date.isBefore(periodBegin)
                    && !operation.date.isAfter(periodEnd)
        })

        Assert.assertTrue(searchResult.zipWithNext().all { pair -> !pair.first.date.isBefore(pair.second.date) })
    }


    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `service properly generates monthly report`() {
        val report = flowService.createReport()

        val sampleStatistics: Map<LocalDate, StatisticsElement> =
            TreeMap(
                mapOf(
                    LocalDate.of(2018, 2, 1) to StatisticsElement(
                        operations = 2,
                        receipt = BigDecimal("1370.00"),
                        expense = BigDecimal("6300.00")
                    ),
                    LocalDate.of(2018, 1, 1) to StatisticsElement(
                        operations = 3,
                        receipt = BigDecimal("37000.00"),
                        expense = BigDecimal("7400.00")
                    )
                )
            )

        assertEquals(sampleStatistics.keys, report.statistics.keys)

        report.statistics.forEach { entry ->
            val sampleValue = sampleStatistics[entry.key]
            assertNotNull(sampleValue)
            with(entry.value) {
                assertEquals(sampleValue?.numOfOperations, numOfOperations)
                assertEquals(sampleValue?.totalReceipt, totalReceipt)
                assertEquals(sampleValue?.totalExpense, totalExpense)
                assertEquals(sampleValue?.balance, balance)
            }
        }
    }
}