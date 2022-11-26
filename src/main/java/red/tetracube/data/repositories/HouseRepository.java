package red.tetracube.data.repositories;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import red.tetracube.data.entities.House;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class HouseRepository {

    private final Mutiny.SessionFactory sessionFactory;

    @Inject
    public HouseRepository(Mutiny.SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Uni<Boolean> existsByName(String name) {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.flatMap(session ->
                session.createQuery("""
                                        select count(house.name) > 0
                                        from House house
                                        where house.name = :name
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
