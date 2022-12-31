package user_payloads

import "github.com/google/uuid"

type UserLoginRequest struct {
	Username           string `json:"username" binding:"required"`
	AuthenticationCode string `json:"authentication_code" binding:"required"`
}

type UserLoginResponse struct {
	BearerToken string    `json:"bearer_token"`
	HouseId     uuid.UUID `json:"house_id"`
	HouseName   string    `json:"house_name"`
}
