package red.tetracube.data.repositories;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jose4j.jwk.Use;
import red.tetracube.data.entities.User;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class UserRepository {

    private final Mutiny.SessionFactory sessionFactory;

    @Inject
    public UserRepository(Mutiny.SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Uni<Boolean> existsByName(String name) {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.flatMap(session ->
                session.createQuery("""
                                        select count(user.name) > 0
                                        from User user
                                        where lower(user.name) = lower(:name)
                                        """,
                                Boolean.class
                        )
                        .setParameter("name", name)
                        .getSingleResultOrNull()
                        .eventually(session::close)
        );
    }

    public Uni<User> save(User user) {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.flatMap(session ->
                session.merge(user)
                        .eventually(session::flush)
                        .eventually(session::close)
        );
    }

    public Uni<Optional<User>> getUserFromAuthenticationToken(String authenticationToken) {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.flatMap(session ->
                session.createQuery("""
                                        select user
                                        from User user
                                        join user.authenticationToken authenticationToken
                                        where authenticationToken.token = :authenticationToken
                                        """,
                                User.class
                        )
                        .setParameter("authenticationToken", authenticationToken)
                        .getSingleResultOrNull()
                        .eventually(session::close)
                        .map(Optional::ofNullable)
        );
    }
}
