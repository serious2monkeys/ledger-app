package ru.doronin.ledgerapp.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import ru.doronin.ledgerapp.user.User
import ru.doronin.ledgerapp.user.UserRole
import ru.doronin.ledgerapp.user.UserService

@Component
class CustomUserDetailsService(private val userService: UserService) : UserDetailsService {

    /**
     * {@inheritDoc}
     */
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userService.findByLogin(username).orElseThrow { UsernameNotFoundException(username) }
        return org.springframework.security.core.userdetails.User(
            user.login, user.password, getGrantedAuthorities(user)
        )
    }

    /**
     * Получение полномочий пользователя
     *
     * @param user - пользователь
     * @return список полномочий (ролей)
     */
    private fun getGrantedAuthorities(user: User): List<GrantedAuthority> {
        val authorities =
            mutableListOf<GrantedAuthority>(SimpleGrantedAuthority("ROLE_OPERATOR"))
        if (UserRole.ADMIN == user.role) {
            authorities.add(SimpleGrantedAuthority("ROLE_ADMIN"))
        }
        return authorities
    }
}