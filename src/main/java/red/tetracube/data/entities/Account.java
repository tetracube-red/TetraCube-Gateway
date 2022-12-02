package red.tetracube.data.entities;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

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

    public Account() {
    }

    public Account(String name, House house, AuthenticationToken authenticationToken) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.house = house;
        this.authenticationToken = authenticationToken;
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
}
