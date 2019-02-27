package ru.doronin.ledgerapp.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun findOneByLogin(login: String): Optional<User>
}