package red.tetracube.data.repositories;

import org.hibernate.reactive.mutiny.Mutiny;

import io.smallrye.mutiny.Uni;
import red.tetracube.data.entities.Permission;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PermissionRepository {

    private final Mutiny.SessionFactory sessionFactory;

    public PermissionRepository(Mutiny.SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Uni<List<Permission>> getAll() {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.chain(session -> {
            return session.createQuery("""
                    from Permission permission
                    """,
                    Permission.class)
                    .getResultList()
                    .eventually(session::close);
        });
    }

}
