package ru.doronin.ledgerapp.operatons

/**
 * Тип движения средств
 */
enum class MovementType(description: String) {
    RECEIPT(description = "Поступление средств"),
    EXPENSE(description = "Расходы")
}