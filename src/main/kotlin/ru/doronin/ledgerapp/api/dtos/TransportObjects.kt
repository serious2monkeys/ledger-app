package ru.doronin.ledgerapp.api.dtos

import ru.doronin.ledgerapp.operatons.MovementType
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime

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