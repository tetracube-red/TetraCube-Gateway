package red.tetracube.data.repositories;

import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AccountRepository {

    private final Mutiny.SessionFactory sessionFactory;

    @Inject
    public AccountRepository(Mutiny.SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
