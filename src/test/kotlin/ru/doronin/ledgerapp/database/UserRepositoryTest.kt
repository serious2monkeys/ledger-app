package ru.doronin.ledgerapp.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit4.SpringRunner
import ru.doronin.ledgerapp.user.User
import ru.doronin.ledgerapp.user.UserRepository
import ru.doronin.ledgerapp.user.UserRole

@RunWith(SpringRunner::class)
@DataJpaTest(showSql = false)
class UserRepositoryTest {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `repository properly finds user`() {
        assertTrue(userRepository.findAll().isEmpty())

        val userName = "testUser"

        val user = with(User(login = userName, password = "testPassword")) {
            lastName = "Тестовый"
            firstName = "Пользователь"
            role = UserRole.ADMIN
            this
        }

        userRepository.save(user)

        val searchResult = userRepository.findOneByLogin(login = userName)
        assertTrue(searchResult.isPresent)
        assertEquals(user, searchResult.get())
    }
}