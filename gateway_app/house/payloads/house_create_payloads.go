package house_payloads

import (
	"github.com/google/uuid"
	"red.tetracube/tetracube-gateway/gateway_app/db/entities"
)

type HouseCreateRequest struct {
	Name string `json:"name" binding:"required"`
}

type HouseCreateResponse struct {
	Id   uuid.UUID `json:"id"`
	Name string    `json:"name"`
}

func MapResponseFromEntity(houseEntity *entities.House) HouseCreateResponse {
	response := HouseCreateResponse{
		Id:   houseEntity.Id,
		Name: houseEntity.Name,
	}
	return response
}
