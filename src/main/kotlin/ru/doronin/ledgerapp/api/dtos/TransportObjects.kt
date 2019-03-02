package ru.doronin.ledgerapp.api.dtos

import com.fasterxml.jackson.annotation.JsonValue
import ru.doronin.ledgerapp.operatons.FlowOperation
import ru.doronin.ledgerapp.operatons.MovementType
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Страница, содержащая данные о наборе операций движения средств
 */
data class FlowsPage(
    val pageNum: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalElements: Long,
    val flows: List<FlowDto>
)

/**
 * Контейнер для передачи данных об операции движения средств
 */
data class FlowDto(
    var id: Long,
    var type: MovementType,
    var description: String,
    var amount: BigDecimal,
    var performerId: Long,
    var modifierId: Long,
    var date: LocalDateTime = LocalDateTime.now(),
    var version: Instant = Instant.now()
)

/**
 * Представление для отчета по месячным операциям
 */
class MonthlyOperationsReport(operations: List<FlowOperation>) {

    @JsonValue
    val statistics: Map<LocalDate, StatisticsElement>

    init {
        val monthlyOperations =
            operations.groupBy { operation -> operation.date.withDayOfMonth(1).toLocalDate() }

        val monthlyStatistics =
            monthlyOperations.mapValues { entry: Map.Entry<LocalDate, List<FlowOperation>> ->
                val receipt = BigDecimal.valueOf(entry.value.asSequence().filter { MovementType.RECEIPT == it.type }
                    .sumByDouble { operation -> operation.amount.toDouble() }).setScale(2, RoundingMode.HALF_UP)

                val expense = BigDecimal.valueOf(entry.value.asSequence().filter { MovementType.EXPENSE == it.type }
                    .sumByDouble { operation -> operation.amount.toDouble() }).setScale(2, RoundingMode.HALF_UP)

                StatisticsElement(entry.value.size, receipt = receipt, expense = expense)
            }

        statistics =
            TreeMap(Comparator { o1: LocalDate, o2: LocalDate -> o2.compareTo(o1) })
        statistics.putAll(monthlyStatistics)
    }
}


class StatisticsElement(operations: Int, receipt: BigDecimal, expense: BigDecimal) {
    val numOfOperations: Int = operations
    val totalReceipt: BigDecimal = receipt
    val totalExpense: BigDecimal = expense
    var balance: BigDecimal = receipt - expense
}