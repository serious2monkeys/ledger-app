package ru.doronin.ledgerapp.operatons

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface FlowRepository : JpaRepository<FlowOperation, Long>, JpaSpecificationExecutor<FlowOperation> {

    fun findAllByDateBetweenOrderByDateDesc(
        start: LocalDateTime,
        end: LocalDateTime
    ): List<FlowOperation>

    @Query(nativeQuery = true, value = "select count(id) from flow_operations")
    fun countRecords(): Long
}