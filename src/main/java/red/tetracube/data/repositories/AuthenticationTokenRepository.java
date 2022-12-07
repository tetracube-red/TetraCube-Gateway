package red.tetracube.data.repositories;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import red.tetracube.data.entities.AuthenticationToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class AuthenticationTokenRepository {

    private final Mutiny.SessionFactory sessionFactory;

    @Inject
    public AuthenticationTokenRepository(Mutiny.SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Uni<AuthenticationToken> save(AuthenticationToken authenticationToken) {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.flatMap(session ->
                session.merge(authenticationToken)
                        .eventually(session::flush)
                        .eventually(session::close)
        );
    }

    public Uni<Optional<AuthenticationToken>> getByToken(String authenticationToken) {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.flatMap(session ->
                session.createQuery("""
                                        from AuthenticationToken authToken
                                        where authToken.token = :token
                                        """,
                                AuthenticationToken.class
                        )
                        .setParameter("token", authenticationToken)
                        .setMaxResults(1)
                        .getSingleResultOrNull()
                        .eventually(session::close)
                        .map(Optional::ofNullable)
        );
    }
}
