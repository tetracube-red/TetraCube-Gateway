package authentication_code_paylads

import (
	"time"

	"red.tetracube/tetracube-gateway/gateway_app/db/entities"
)

type CreateAuthenticationCodeRequest struct {
	HouseName string `json:"house_name" binding:"required"`
}

type CreateAuthenticationCodeResponse struct {
	Code       string `json:"code"`
	ValidUntil time.Time
}

func MapResponseFromEntity(authenticationToken *entities.AuthenticationToken) CreateAuthenticationCodeResponse {
	response := CreateAuthenticationCodeResponse{
		Code:       authenticationToken.Code,
		ValidUntil: authenticationToken.ValidUntil,
	}
	return response
}
