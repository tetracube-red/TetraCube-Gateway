package red.tetracube.data.repositories;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import red.tetracube.data.entities.Authorization;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class AuthorizationRepository {

    private final Mutiny.SessionFactory sessionFactory;

    public AuthorizationRepository(Mutiny.SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    public Uni<List<Authorization>> getAll() {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.flatMap(session ->
                session.createQuery("""
                                        from Authorization authorization
                                        """,
                                Authorization.class)
                        .getResultList()
                        .eventually(session::close)
        );
    }
}
