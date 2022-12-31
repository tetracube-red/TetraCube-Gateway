package red.tetracube.data.entities;

import javax.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "gateway", name = "tetracubes")
public class TetraCube {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tetracube", targetEntity = Guest.class)
    private List<Guest> guestList;

    public TetraCube() {
    }

    public TetraCube(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Guest> getGuestList() {
        return guestList;
    }

    public void setGuestList(List<Guest> guestList) {
        this.guestList = guestList;
    }
    
}
