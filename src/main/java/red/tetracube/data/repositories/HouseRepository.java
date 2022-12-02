package red.tetracube.data.repositories;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import red.tetracube.data.entities.House;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class HouseRepository {

    private final Mutiny.SessionFactory sessionFactory;

    @Inject
    public HouseRepository(Mutiny.SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Uni<House> getById(UUID id) {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.flatMap(session ->
                session.createQuery("""
                                        from House house
                                        where house.id = :id
                                        """,
                                House.class
                        )
                        .setParameter("id", id)
                        .getSingleResultOrNull()
                        .eventually(session::close)
        );
    }

    public Uni<Boolean> existsByName(String name) {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.flatMap(session ->
                session.createQuery("""
                                        select count(house.name) > 0
                                        from House house
                                        where lower(house.name) = lower(:name)
                                        """,
                                Boolean.class
                        )
                        .setParameter("name", name)
                        .getSingleResultOrNull()
                        .eventually(session::close)
        );
    }

    public Uni<House> save(House house) {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.flatMap(session ->
                session.merge(house)
                        .eventually(session::flush)
                        .eventually(session::close)
        );
    }
}
