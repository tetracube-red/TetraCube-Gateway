package red.tetracube.data.entities;

import red.tetracube.core.enumerations.AuthorizationName;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "gateway", name = "permissions")
public class Permission {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private AuthorizationName name;

    @ManyToMany(
            targetEntity = Guest.class,
            fetch = FetchType.LAZY,
            mappedBy = "permissionList"
    )
    private List<Guest> guestList;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AuthorizationName getName() {
        return name;
    }

    public void setName(AuthorizationName name) {
        this.name = name;
    }

    public List<Guest> getGuestList() {
        return guestList;
    }

    public void setGuestList(List<Guest> guestList) {
        this.guestList = guestList;
    }

}
