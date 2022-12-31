package entities

import (
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/uptrace/bun"
)

type AuthenticationToken struct {
	bun.BaseModel `bun:"table:gateway.authentication_tokens,alias:at"`

	Id         uuid.UUID `bun:"id,pk,type:uuid,default:uuid_generate_v4()"`
	Code       string    `bun:"token,unique,notnull"`
	ValidFrom  time.Time `bun:"valid_from,notnull"`
	ValidUntil time.Time `bun:"valid_until,notnull"`
	IsValid    bool      `bun:"is_valid,notnull,default:true"`
	InUse      bool      `bun:"in_use,notnull,default:false"`
	HouseId    uuid.UUID `bun:"house_id,type:uuid,notnull"`
	House      *House    `bun:"rel:belongs-to,join:house_id=id"`
	Users      []*User   `bun:"rel:has-many,join:id=id_authentication_token"`
}

func GetNewAuthenticationTokenEntity(houseId uuid.UUID) *AuthenticationToken {
	randomUUID, _ := uuid.NewUUID()
	code := strings.ReplaceAll(randomUUID.String(), "-", "")[0:6]

	authenticationToken := AuthenticationToken{
		Id:         uuid.New(),
		Code:       code,
		ValidFrom:  time.Now(),
		ValidUntil: time.Now().AddDate(1, 0, 0),
		IsValid:    true,
		InUse:      false,
		HouseId:    houseId,
	}
	return &authenticationToken
}

/*

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
*/
