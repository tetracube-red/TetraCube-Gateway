package entities

import (
	"github.com/google/uuid"
	"github.com/uptrace/bun"
)

type House struct {
	bun.BaseModel        `bun:"table:gateway.houses,alias:h"`
	Id                   uuid.UUID              `bun:"id,pk,type:uuid,default:uuid_generate_v4()"`
	Name                 string                 `bun:"name,unique,notnull"`
	AuthenticationTokens []*AuthenticationToken `bun:"rel:has-many,join:id=house_id"`
	Users                []*User                `bun:"rel:has-many,join:id=id_house"`
}
