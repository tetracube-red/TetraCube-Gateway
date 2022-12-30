package house

import (
	"context"
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"red.tetracube/tetracube-gateway/gateway_app/db"
	"red.tetracube/tetracube-gateway/gateway_app/db/entities"
	house_payloads "red.tetracube/tetracube-gateway/gateway_app/house/payloads"
)

type HouseController struct {
	DbConnection *db.DatabaseConnection
}

func (houseController *HouseController) CreateHouse(c *gin.Context) {
	body := house_payloads.HouseCreateRequest{}
	if err := c.BindJSON(&body); err != nil {
		c.AbortWithError(http.StatusBadRequest, err)
		return
	}

	ctx := context.Background()
	house := &entities.House{
		Id:   uuid.New(),
		Name: body.Name,
	}
	res, err := houseController.DbConnection.DB.NewInsert().Model(house).Exec(ctx)
	if err != nil {
		fmt.Printf("Error in house creation due error %s\n", err.Error())
		c.AbortWithError(http.StatusInternalServerError, err)
		return
	}

	rowsAffected, _ := res.RowsAffected()
	fmt.Printf("Inserted %d rows with id %s\n", rowsAffected, house.Id)

	houseResponse := house_payloads.MapResponseFromEntity(house)
	c.JSON(http.StatusOK, houseResponse)
}
