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

    /*
     * 
     * 
     * public Uni<Guest> getUserFromAuthenticationCode(String authenticationCode) {
     * var sessionUni = sessionFactory.openSession();
     * return sessionUni.flatMap(session ->
     * session.createQuery("""
     * select user
     * from User user
     * left join user.authenticationToken authenticationToken
     * left join fetch user.authorizationList authorizations
     * where authenticationToken.token = :authenticationCode
     * """,
     * Guest.class
     * )
     * .setParameter("authenticationCode", authenticationCode)
     * .getSingleResultOrNull()
     * .eventually(session::close)
     * );
     * }
     */
}
