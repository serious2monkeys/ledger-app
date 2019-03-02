package ru.doronin.ledgerapp.database

import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.spring.api.DBRider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import ru.doronin.ledgerapp.operatons.FlowRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@RunWith(SpringRunner::class)
@SpringBootTest
@DBRider
class FlowsRepositoryTest {
    @Autowired
    private lateinit var flowRepository: FlowRepository

    @Test
    @DataSet(
        cleanBefore = true,
        cleanAfter = true,
        value = ["employees.yml", "users.yml", "flows.yml"]
    )
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

        assertTrue(searchResult.all { operation ->
            !operation.date.isBefore(periodBegin)
                    && !operation.date.isAfter(periodEnd)
        })

        assertTrue(searchResult.zipWithNext().all { pair -> !pair.first.date.isBefore(pair.second.date) })
    }
}