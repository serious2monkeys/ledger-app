package ru.doronin.ledgerapp.operatons

import org.hibernate.annotations.Type
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import ru.doronin.ledgerapp.employee.Employee
import ru.doronin.ledgerapp.user.User
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import javax.persistence.*

/**
 * Операция движения средств
 */
@Entity
@Table(
    name = "FLOW_OPERATIONS",
    uniqueConstraints = [UniqueConstraint(name = "UC_FLOWS_EMPLOYEE_DATE", columnNames = ["empl_id", "date"])]
)
data class FlowOperation(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long? = null,

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: MovementType = MovementType.RECEIPT,

    @Column(name = "description", nullable = false)
    @Type(type = "org.hibernate.type.TextType")
    var description: String = "",

    @Column(name = "amount", nullable = false)
    var amount: BigDecimal = BigDecimal.ZERO,

    @ManyToOne(targetEntity = Employee::class)
    @JoinColumn(name = "empl_id", foreignKey = ForeignKey(name = "FK_EMPLOYEE_OPERATION"))
    var performer: Employee = Employee(),

    @ManyToOne(targetEntity = User::class)
    @JoinColumn(name = "modifier_id", foreignKey = ForeignKey(name = "FK_USER_OPERATION"))
    @LastModifiedBy
    var modifiedBy: User,

    @Column(name = "date")
    var date: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "version")
    var version: Instant = Instant.now()
)