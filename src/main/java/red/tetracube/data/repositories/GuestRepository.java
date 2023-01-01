package red.tetracube.data.repositories;

import org.hibernate.reactive.mutiny.Mutiny;

import io.smallrye.mutiny.Uni;
import red.tetracube.data.entities.Guest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class GuestRepository {

    private final Mutiny.SessionFactory sessionFactory;

    @Inject
    public GuestRepository(Mutiny.SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Uni<Boolean> existsByName(String nickname) {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.chain(session -> session.createQuery("""
                select count(guest.nickname) > 0
                from Guest guest
                where lower(guest.nickname) = lower(:nickname)
                """,
                Boolean.class)
                .setParameter("nickname", nickname)
                .getSingleResultOrNull()
                .eventually(session::close));
    }

    public Uni<Guest> save(Guest user) {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.chain(session -> session.merge(user)
                .eventually(session::flush)
                .eventually(session::close));
    }

    public Uni<Guest> getByNickname(String nickname) {
        var sessionUni = sessionFactory.openSession();
        return sessionUni.chain(session -> {
            return session.createQuery("""
                    select guest
                    from Guest guest
                    left join fetch guest.permissionList permissions
                    left join fetch guest.tetracube tetracube
                    where guest.nickname = :nickname
                    """,
                    Guest.class)
                    .setParameter("nickname", nickname)
                    .getSingleResultOrNull()
                    .eventually(session::close);
        });
    }

}
