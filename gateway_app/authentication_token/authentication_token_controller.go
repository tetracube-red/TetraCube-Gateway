package authentication_token

import (
	"context"
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
	authentication_code_paylads "red.tetracube/tetracube-gateway/gateway_app/authentication_token/payloads"
	"red.tetracube/tetracube-gateway/gateway_app/db"
	"red.tetracube/tetracube-gateway/gateway_app/db/entities"
)

type AuthenticationTokenController struct {
	DbConnection *db.DatabaseConnection
}

func (authenticationTokenController *AuthenticationTokenController) CreateAuthenticationToken(c *gin.Context) {
	body := authentication_code_paylads.CreateAuthenticationCodeRequest{}
	if err := c.BindJSON(&body); err != nil {
		c.AbortWithError(http.StatusBadRequest, err)
		return
	}

	ctx := context.Background()
	house := new(entities.House)
	houseCount, err := authenticationTokenController.DbConnection.DB.NewSelect().Model(house).Where("name = ?", body.HouseName).ScanAndCount(ctx)
	if houseCount == 0 {
		fmt.Printf("House name %s does not exist\n", body.HouseName)
		c.AbortWithError(http.StatusNotFound, err)
	}
	if err != nil {
		fmt.Printf("Cannot retrieve house by name due error: %s\n", err.Error())
		c.AbortWithError(http.StatusInternalServerError, err)
		return
	}

	authenticationTokenEntity := entities.GetNewAuthenticationTokenEntity(house.Id)
	_, atStoreErr := authenticationTokenController.DbConnection.DB.NewInsert().Model(authenticationTokenEntity).Exec(ctx)
	if atStoreErr != nil {
		fmt.Printf("Cannot store authentication token due error: %s\n", err.Error())
		c.AbortWithError(http.StatusInternalServerError, err)
		return
	}
	response := authentication_code_paylads.MapResponseFromEntity(authenticationTokenEntity)
	c.JSON(http.StatusOK, response)
}
