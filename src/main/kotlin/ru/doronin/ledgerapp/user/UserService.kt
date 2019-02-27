package ru.doronin.ledgerapp.user

import org.springframework.context.annotation.Bean
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UserService(private val userRepository: UserRepository) {

    fun findByLogin(login: String) = userRepository.findOneByLogin(login)

    @Transactional
    fun save(user: User): User {
        user.id = user.id ?: userRepository.findOneByLogin(user.login).orElse(null)?.id
        user.password = encodePassword(user)
        return userRepository.save(user)
    }

    @Bean
    fun encoder(): PasswordEncoder = BCryptPasswordEncoder(11)

    /**
     * Получение текущего пользователя
     */
    @PreAuthorize(value = "hasRole('OPERATOR')")
    fun getCurrent(): User =
        findByLogin(SecurityContextHolder.getContext().authentication.name)
            .orElseThrow { IllegalStateException("Failed to determine authenticated user") }

    /**
     * Кодирует пароль для сохранения в БД
     *
     * @param user - пользователь
     * закодированный пароль
     */
    private fun encodePassword(user: User): String {
        val id = user.id ?: return encoder().encode(user.password)

        val userOptional = userRepository.findById(id)

        return if (userOptional.get().password != user.password) {
            encoder().encode(user.password)
        } else user.password
    }
}