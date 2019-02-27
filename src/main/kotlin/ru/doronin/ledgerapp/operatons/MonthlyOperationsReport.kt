package ru.doronin.ledgerapp.operatons

import com.fasterxml.jackson.annotation.JsonValue
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.*
import kotlin.Comparator

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