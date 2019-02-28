package ru.doronin.ledgerapp.employee

import ru.doronin.ledgerapp.general.Person
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table
import javax.validation.constraints.NotBlank

/**
 * Сотрудник
 */
@Entity
@Table(name = "EMPLOYEES")
data class Employee(

    @Column(name = "position", nullable = false)
    @NotBlank(message = "Cannot be blank")
    var position: String = "Менеджер"

) : Person()
