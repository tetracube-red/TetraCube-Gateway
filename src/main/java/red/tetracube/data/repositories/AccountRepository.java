package red.tetracube.data.repositories;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import red.tetracube.data.entities.Account;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AccountRepository {

    private final Mutiny.SessionFactory sessionFactory;

    @Inject
    public AccountRepository(Mutiny.SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Uni<Boolean> existsByName(String name) {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.flatMap(session ->
                session.createQuery("""
                                        select count(account.name) > 0
                                        from Account account
                                        where lower(account.name) = lower(:name)
                                        """,
                                Boolean.class
                        )
                        .setParameter("name", name)
                        .getSingleResultOrNull()
                        .eventually(session::close)
        );
    }

    public Uni<Account> save(Account account) {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.flatMap(session ->
                session.merge(account)
                        .eventually(session::flush)
                        .eventually(session::close)
        );
    }
}
