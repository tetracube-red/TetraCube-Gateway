package entities

import (
	"github.com/google/uuid"
	"github.com/uptrace/bun"
)

type User struct {
	bun.BaseModel         `bun:"table:gateway.users,alias:u"`
	Id                    uuid.UUID            `bun:"id,pk,type:uuid,default:uuid_generate_v4()"`
	Name                  string               `bun:"name,unique,notnull`
	HouseId               uuid.UUID            `bun:"id_house,type:uuid,notnull"`
	House                 *House               `bun:"rel:belongs-to,join:id_house=id"`
	AuthenticationTokenId uuid.UUID            `bun:"id_authentication_token,type:uuid,notnull"`
	AuthenticationToken   *AuthenticationToken `bun:"rel:belongs-to,join:id_authentication_token=id"`
}
