package red.tetracube.data.repositories;

import org.hibernate.reactive.mutiny.Mutiny;

import io.smallrye.mutiny.Uni;
import red.tetracube.data.entities.TetraCube;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TetraCubeRepository {

    private final Mutiny.SessionFactory sessionFactory;

    @Inject
    public TetraCubeRepository(Mutiny.SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Uni<Boolean> existsByName(String name) {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.chain(session -> session.createQuery("""
                select count(tetracube.name) > 0
                from TetraCube tetracube
                where lower(tetracube.name) = lower(:name)
                """,
                Boolean.class)
                .setParameter("name", name)
                .getSingleResultOrNull()
                .eventually(session::close));
    }

    public Uni<TetraCube> save(TetraCube tetraCube) {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.chain(session -> session.merge(tetraCube)
                .eventually(session::flush)
                .eventually(session::close));
    }

    public Uni<TetraCube> getByName(String name) {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.chain(session -> session.createQuery("""
                from TetraCube tetracube
                where tetracube.name = :name
                """,
                TetraCube.class)
                .setParameter("name", name)
                .getSingleResultOrNull()
                .eventually(session::close));
    }

    /*
     * 
     * 
     * public Uni<Optional<TetraCube>> getById(UUID id) {
     * var sessionUni = sessionFactory.openSession();
     * return sessionUni.flatMap(session ->
     * session.createQuery("""
     * from House house
     * where house.id = :id
     * """,
     * TetraCube.class
     * )
     * .setParameter("id", id)
     * .getSingleResultOrNull()
     * .eventually(session::close)
     * .map(Optional::ofNullable)
     * );
     * }
     * 
     * public Uni<TetraCube> getByRelatedAuthenticationCode(String
     * authenticationCode) {
     * var sessionUni = sessionFactory.openSession();
     * return sessionUni.flatMap(session ->
     * session.createQuery("""
     * select house
     * from House house
     * left join house.authenticationTokenList authenticationTokens
     * where authenticationTokens.token = :authenticationCode
     * """,
     * TetraCube.class
     * )
     * .setParameter("authenticationCode", authenticationCode)
     * .getSingleResultOrNull()
     * .eventually(session::close)
     * );
     * }
     */
}
