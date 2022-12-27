package red.tetracube.data.entities;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;

@Entity
@Table(schema = "gateway", name = "authentication_tokens")
public class AuthenticationToken {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "valid_from", nullable = false)
    private Timestamp validFrom;

    @Column(name = "valid_until", nullable = false)
    private Timestamp validUntil;

    @Column(name = "is_valid", nullable = false)
    private Boolean isValid;

    @Column(name = "in_use", nullable = false)
    private Boolean inUse;

    @JoinColumn(name = "id_house", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = House.class)
    private House house;

    public AuthenticationToken() {
    }

    public AuthenticationToken(String token, House house) {
        var now = Calendar.getInstance();
        var validUntil = Calendar.getInstance();
        validUntil.add(Calendar.YEAR, 1);
        this.id = UUID.randomUUID();
        this.token = token;
        this.validFrom = new Timestamp(now.getTimeInMillis());
        this.validUntil = new Timestamp(validUntil.getTimeInMillis());
        this.isValid = true;
        this.inUse = false;
        this.house = house;
    }

    public String getToken() {
        return token;
    }

    public Timestamp getValidUntil() {
        return validUntil;
    }

    public Boolean getInUse() {
        return inUse;
    }

    public void setAsInUse() {
        this.inUse = true;
    }

    public House getHouse() {
        return house;
    }
}
