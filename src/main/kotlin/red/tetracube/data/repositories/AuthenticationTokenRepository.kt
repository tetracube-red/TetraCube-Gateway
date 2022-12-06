package red.tetracube.data.repositories;

import io.smallrye.mutiny.coroutines.awaitSuspending
import org.hibernate.reactive.mutiny.Mutiny
import red.tetracube.data.entities.AuthenticationToken
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class AuthenticationTokenRepository(
    private val sessionFactory: Mutiny.SessionFactory
) {

    suspend fun save(authenticationToken: AuthenticationToken): AuthenticationToken {
        val sessionUni = sessionFactory.openSession()
        return sessionUni.flatMap { session ->
            session.merge(authenticationToken)
                .eventually(session::flush)
                .eventually(session::close)
        }
            .awaitSuspending()
    }

    suspend fun getByToken(authenticationToken: String): AuthenticationToken? {
        val sessionUni = sessionFactory.openSession()
        return sessionUni.flatMap { session ->
            session.createQuery(
                """
                                        from AuthenticationToken authToken
                                        join fetch authToken.house
                                        where authToken.token = :token
                                        """,
                AuthenticationToken::class.java
            )
                .setParameter("token", authenticationToken)
                .setMaxResults(1)
                .singleResultOrNull
                .eventually(session::close)
        }
            .awaitSuspending()
    }
}
