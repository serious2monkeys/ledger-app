package ru.doronin.ledgerapp.user

import com.fasterxml.jackson.annotation.JsonFilter
import ru.doronin.ledgerapp.general.Person
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

/**
 * Сущность пользователя
 */
@Entity
@Table(name = "USERS")
@JsonFilter("ignoringPassword")
data class User(

    @Column(name = "login", nullable = false, unique = true)
    @NotBlank(message = "Cannot be blank")
    @Size(min = 5, message = "Should be at least 5 characters long")
    var login: String,

    @Column(name = "password", nullable = false)
    @NotBlank(message = "Cannot be blank")
    @Size(min = 5, message = "Should be at least 5 characters long")
    var password: String,

    @Enumerated(value = EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: UserRole = UserRole.OPERATOR
) : Person()