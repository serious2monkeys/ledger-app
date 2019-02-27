package ru.doronin.ledgerapp.employee

import com.fasterxml.jackson.annotation.JsonAutoDetect
import ru.doronin.ledgerapp.general.Person
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table
import javax.validation.constraints.NotBlank

/**
 * Работник
 */
@Entity
@Table(name = "EMPLOYEES")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class Employee(

    @Column(name = "position", nullable = false)
    @NotBlank(message = "Cannot be blank")
    var position: String = "Менеджер"

) : Person()
