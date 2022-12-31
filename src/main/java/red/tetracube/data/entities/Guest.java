package red.tetracube.data.entities;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "gateway", name = "guests")
public class Guest {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "password", nullable = false)
    private String password;

    @JoinColumn(name = "tetracube_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = TetraCube.class)
    private TetraCube tetracube;

    @ManyToMany(
            targetEntity = Permission.class,
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE}
    )
    @JoinTable(
            schema = "gateway",
            name = "guests_permissions",
            joinColumns = {@JoinColumn(name = "guest_id")},
            inverseJoinColumns = {@JoinColumn(name = "permission_id")}
    )
    private List<Permission> permissionList = new ArrayList<>();

    public Guest() {
    }

    public Guest(String nickname, String password, TetraCube tetracube) {
        this.id = UUID.randomUUID();
        this.password = password;
        this.nickname = nickname;
        this.tetracube = tetracube;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public TetraCube getTetracube() {
        return tetracube;
    }

    public void setTetracube(TetraCube tetracube) {
        this.tetracube = tetracube;
    }

    public List<Permission> getPermissionList() {
        return permissionList;
    }

    public void setPermissionList(List<Permission> permissionList) {
        this.permissionList = permissionList;
    }
    
}
