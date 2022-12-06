package red.tetracube.data.repositories;

import io.smallrye.mutiny.coroutines.awaitSuspending
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory
import red.tetracube.data.entities.House
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class HouseRepository(
    private val sessionFactory: SessionFactory
) {

    suspend fun getByName(name: String): House? {
        val sessionUni = sessionFactory.openSession()
        return sessionUni.flatMap { session ->
            session.createQuery(
                """
                    from House house
                    where house.name = :name
                """,
                House::class.java
            )
                .setParameter("name", name)
                .singleResultOrNull
                .eventually(session::close)
        }
            .awaitSuspending()
    }

    suspend fun getById(id: UUID): House? {
        val sessionUni = sessionFactory.openSession()
        return sessionUni.flatMap { session ->
            session.createQuery(
                """
                    from House house
                    where house.id = :id
                """,
                House::class.java
            )
                .setParameter("id", id)
                .singleResultOrNull
                .eventually(session::close)
        }
            .awaitSuspending()
    }


    suspend fun existsByName(name: String): Boolean {
        val sessionUni = sessionFactory.openSession();
        return sessionUni.flatMap { session ->
            session.createQuery(
                """
                    select count(house.name) > 0
                    from House house
                    where lower(house.name) = lower(:name)
                """,
                Boolean::class.java
            )
                .setParameter("name", name)
                .singleResult
                .eventually(session::close)
        }
            .awaitSuspending()
    }

    suspend fun save(house: House): House {
        val sessionUni = sessionFactory.openSession()
        return sessionUni.flatMap { session ->
            session.merge(house)
                .eventually(session::flush)
                .eventually(session::close)
        }
            .awaitSuspending()
    }
}
