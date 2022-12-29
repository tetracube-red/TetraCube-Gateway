package red.tetracube.data.entities;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "gateway", name = "houses")
public class House {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @OneToMany(fetch = FetchType.LAZY, targetEntity = AuthenticationToken.class, mappedBy = "house")
    private List<AuthenticationToken> authenticationTokenList;

    public House() {
    }

    public House(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
