package red.tetracube.data.repositories;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import red.tetracube.data.entities.AuthenticationToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
}
