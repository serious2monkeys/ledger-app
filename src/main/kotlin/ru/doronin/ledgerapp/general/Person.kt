package ru.doronin.ledgerapp.general

import javax.persistence.*
import javax.validation.constraints.NotBlank

/**
 * Общий вид записи о человеке
 */
@MappedSuperclass
abstract class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long? = null

    @Column(name = "first_name", nullable = false)
    @NotBlank(message = "Cannot be blank")
    lateinit var firstName: String

    @Column(name = "last_name", nullable = false)
    @NotBlank(message = "Cannot be blank")
    lateinit var lastName: String

    @Column(name = "patronymic")
    var patronymic: String? = null
}