package ru.doronin.ledgerapp.operatons

import au.com.console.jpaspecificationdsl.equal
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import ru.doronin.ledgerapp.api.dtos.MonthlyOperationsReport
import ru.doronin.ledgerapp.employee.Employee
import ru.doronin.ledgerapp.user.User
import ru.doronin.ledgerapp.user.UserRole
import java.time.LocalDate
import java.time.LocalDateTime
import javax.transaction.Transactional

/**
 * Служба работы с движениями средств
 */
@Service
class FlowService(private val flowRepository: FlowRepository) {

    fun findById(id: Long) = flowRepository.findById(id)

    fun findAll(beginDateTime: LocalDateTime? = null, endDateTime: LocalDateTime? = null): List<FlowOperation> {

        if (beginDateTime == null && endDateTime == null) {
            flowRepository.findAll()
        }

        val begin = beginDateTime ?: LocalDate.of(1, 1, 1).atStartOfDay()
        val end = endDateTime ?: LocalDateTime.now()

        return flowRepository.findAllByDateBetweenOrderByDateDesc(begin, end)
    }

    /**
     * Поиск по исполнителю и дате исполнения
     *
     * @param employee Employee - исполнитель
     * @param dateTime LocalDateTime - дата
     */
    private fun findByPerformer(employee: Employee, dateTime: LocalDateTime) =
        flowRepository.findOne(
            Specification.where(FlowOperation::performer.equal(employee)).and(
                FlowOperation::date.equal(dateTime)
            )
        )

    /**
     * Поиск движений за период
     *
     * @param performer Employee? - исполнитель
     * @param modifier User? - пользователь, внесший запись
     * @param begin LocalDate - начало периода
     * @param end LocalDate - конец периода
     * @return List<FlowOperation> - список операций
     */
    fun findByCriteria(
        page: Int,
        pageSize: Int,
        performer: Employee?,
        modifier: User?
    ): Page<FlowOperation> {

        val pageRequest = PageRequest.of(page, pageSize, Sort(Sort.Direction.DESC, "date"))

        if (performer == null && modifier == null) {
            return flowRepository.findAll(pageRequest)
        }

        var specification: Specification<FlowOperation>? = null

        performer?.let { employee -> specification = Specification.where(FlowOperation::performer.equal(employee)) }
        modifier?.let { user ->
            specification = (specification?.and(FlowOperation::modifiedBy.equal(user)))
                ?: Specification.where(FlowOperation::modifiedBy.equal(user))
        }

        return flowRepository.findAll(specification, pageRequest)
    }

    /**
     * Подсчет количества записей
     */
    fun countRecords() = flowRepository.countRecords()

    /**
     * Сохранение записи о движении средств
     *
     * @param flow FlowOperation - запись
     */
    @Transactional
    fun save(flow: FlowOperation): FlowOperation {
        val previous: FlowOperation? = findByPerformer(flow.performer, flow.date).orElse(null)
        if ((previous != null) && (UserRole.ADMIN != flow.modifiedBy.role) && (flow.modifiedBy.id != previous.modifiedBy.id)) {
            throw AccessDeniedException("Access denied")
        }
        flow.id = flow.id ?: previous?.id
        return flowRepository.save(flow)
    }

    /**
     * Формирование помесячного отчета о движениях средств
     *
     * @param begin LocalDateTime   - начало периода выборки
     * @param end LocalDateTime     - конец периода выборки
     */
    @PreAuthorize("hasRole('ADMIN')")
    fun createReport(begin: LocalDateTime? = null, end: LocalDateTime? = null): MonthlyOperationsReport =
        MonthlyOperationsReport(findAll(begin, end))

    @Transactional
    fun delete(flow: FlowOperation) = flowRepository.delete(flow)
}