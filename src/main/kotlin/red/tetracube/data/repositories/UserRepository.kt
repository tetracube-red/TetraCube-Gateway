package red.tetracube.data.repositories;

import io.smallrye.mutiny.coroutines.awaitSuspending
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory
import red.tetracube.data.entities.User
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class UserRepository(
    private val sessionFactory: SessionFactory
) {

    suspend fun existsByName(name: String): Boolean {
        val sessionUni = sessionFactory.openSession()
        return sessionUni.flatMap { session ->
            session.createQuery(
                """
                    select count(user.name) > 0
                    from User user
                    where lower(user.name) = lower(:name)
                """,
                Boolean::class.java
            )
                .setParameter("name", name)
                .singleResult
                .eventually(session::close)
        }
            .awaitSuspending()
    }

    suspend fun save(user: User): User {
        val sessionUni = sessionFactory.openSession()
        return sessionUni.flatMap { session ->
            session.merge(user)
                .eventually(session::flush)
                .eventually(session::close)
        }
            .awaitSuspending()
    }

    suspend fun getUserFromAuthenticationToken(authenticationToken: String): User? {
        val sessionUni = sessionFactory.openSession()
        return sessionUni.flatMap { session ->
            session.createQuery(
                """
                    select user
                    from User user
                    join user.authenticationToken authenticationToken
                    where authenticationToken.token = :token
                """,
                User::class.java
            )
                .setParameter("token", authenticationToken)
                .singleResultOrNull
                .eventually(session::close)
        }
            .awaitSuspending()
    }
}
