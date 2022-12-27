package red.tetracube.data.entities;

import red.tetracube.core.enumerations.AuthorizationName;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "gateway", name = "authorizations")
public class Authorization {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private AuthorizationName name;

    @ManyToMany(
            targetEntity = User.class,
            fetch = FetchType.LAZY,
            mappedBy = "authorizationList"
    )
    private List<User> userList;
}
