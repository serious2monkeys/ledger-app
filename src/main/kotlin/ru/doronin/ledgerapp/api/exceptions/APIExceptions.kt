package ru.doronin.ledgerapp.api.exceptions

class EmployeeNotFoundException(id: Long) : RuntimeException("Employee not found with id = $id")

class FlowOperationNotFoundException(id: Long) : RuntimeException("Flow operation with id = $id not found")