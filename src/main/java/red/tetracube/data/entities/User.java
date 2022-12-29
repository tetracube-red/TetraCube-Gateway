package red.tetracube.data.entities;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "gateway", name = "users")
public class User {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @JoinColumn(name = "id_house", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = House.class)
    private House house;

    @JoinColumn(name = "id_authentication_token")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = AuthenticationToken.class)
    private AuthenticationToken authenticationToken;

    @ManyToMany(
            targetEntity = Authorization.class,
            fetch = FetchType.LAZY
    )
    @JoinTable(
            schema = "gateway",
            name = "users_authorizations",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "authorization_id")}
    )
    private List<Authorization> authorizationList;

    public User() {
    }

    public User(String name, House house, AuthenticationToken authenticationToken, List<Authorization> authorizationList) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.house = house;
        this.authenticationToken = authenticationToken;
        this.authorizationList = authorizationList;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public AuthenticationToken getAuthenticationToken() {
        return authenticationToken;
    }

    public House getHouse() {
        return house;
    }
}
