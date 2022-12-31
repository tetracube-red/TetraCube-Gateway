package user

import (
	"context"
	"fmt"
	"io"
	"net/http"
	"os"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt"
	"github.com/google/uuid"
	"red.tetracube/tetracube-gateway/gateway_app/db"
	"red.tetracube/tetracube-gateway/gateway_app/db/entities"
	user_payloads "red.tetracube/tetracube-gateway/gateway_app/user/payloads"
)

type UserController struct {
	DbConnection *db.DatabaseConnection
}

func (userController *UserController) UserLogin(c *gin.Context) {
	body := user_payloads.UserLoginRequest{}
	if err := c.BindJSON(&body); err != nil {
		c.AbortWithError(http.StatusBadRequest, err)
		return
	}

	ctx := context.Background()
	fmt.Printf("Checking if authentication code %s exists\n", body.AuthenticationCode)
	authenticationToken := new(entities.AuthenticationToken)
	authenticationTokensCount, authTokenCountErr := userController.DbConnection.DB.NewSelect().
		Model(authenticationToken).
		Relation("House").
		Where("token = ?", body.AuthenticationCode).
		ScanAndCount(ctx)

	fmt.Printf("Validating user login data\n")
	if authenticationTokensCount == 0 {
		fmt.Printf("Token not found\n")
		c.AbortWithError(http.StatusNotFound, authTokenCountErr)
	}
	if authTokenCountErr != nil {
		fmt.Printf("Cannot retrieve house by name due error: %s\n", authTokenCountErr.Error())
		c.AbortWithError(http.StatusInternalServerError, authTokenCountErr)
		return
	}
	if authenticationToken.ValidUntil.Before(time.Now()) {
		fmt.Printf("Token expired\n")
		c.AbortWithError(http.StatusBadRequest, nil)
		return
	}

	userExistsByUsername, userExistsByUsernameErr := userController.DbConnection.DB.NewSelect().Model((*entities.User)(nil)).Exists(ctx)
	if userExistsByUsernameErr != nil {
		fmt.Printf("Cannot check if user with username exists due error %s\n", userExistsByUsernameErr.Error())
		c.AbortWithError(http.StatusInternalServerError, userExistsByUsernameErr)
		return
	}

	userEntity := new(entities.User)
	userByAuthenticationTokenCount, userByAuthenticationTokenErr := userController.DbConnection.DB.NewSelect().
		Model(userEntity).
		Join("JOIN gateway.authentication_tokens AS at").
		JoinOn("at.id = u.id_authentication_token").
		Where("at.token = ?", body.AuthenticationCode).
		ScanAndCount(ctx)
	fmt.Printf("Found %d users related to the token\n", userByAuthenticationTokenCount)

	if userByAuthenticationTokenCount == 0 && userExistsByUsername {
		fmt.Printf("Username already exists\n")
		c.AbortWithError(http.StatusConflict, userByAuthenticationTokenErr)
		return
	}
	if userByAuthenticationTokenCount == 1 && userEntity.Name != body.Username {
		fmt.Printf("The user related to the token is present, but is not the same to the name supplied by application\n")
		c.AbortWithError(http.StatusUnauthorized, userByAuthenticationTokenErr)
	}
	if userByAuthenticationTokenCount == 0 && !userExistsByUsername {
		userEntity.Id = uuid.New()
		userEntity.Name = body.Username
		userEntity.HouseId = authenticationToken.HouseId
		userEntity.AuthenticationTokenId = authenticationToken.Id
		_, userEntityInsertError := userController.DbConnection.DB.NewInsert().Model(userEntity).Exec(ctx)
		if userEntityInsertError != nil {
			fmt.Printf("Cannot insert the user, aborting the login procedure\n")
			c.AbortWithError(http.StatusInternalServerError, userEntityInsertError)
		}

		authenticationToken.InUse = true
		_, authenticationTokenUpdateErr := userController.DbConnection.DB.NewUpdate().
			Model(authenticationToken).
			Column("in_use").
			Where("id = ?", authenticationToken.Id).
			Exec(ctx)
		if authenticationTokenUpdateErr != nil {
			fmt.Println("Cannot update authentication as in_use")
			c.AbortWithError(http.StatusInternalServerError, authenticationTokenUpdateErr)
		}
	}

	bearerToken, bearerTokenErr := userController.signToken(userEntity.Name, authenticationToken.ValidUntil)
	if bearerTokenErr != nil {
		fmt.Printf("Cannot generate bearer token due error %s\n", bearerTokenErr.Error())
	}
	response := user_payloads.UserLoginResponse{
		BearerToken: *bearerToken,
		HouseId:     authenticationToken.HouseId,
		HouseName:   authenticationToken.House.Name,
	}
	c.JSON(http.StatusOK, response)
}

func (userController *UserController) signToken(username string, tokenExpiration time.Time) (*string, error) {
	fmt.Println("Building JWT claims")
	claims := jwt.MapClaims{}
	claims["nickname"] = username
	claims["iat"] = time.Now()
	claims["issuer"] = "red.tetracube"
	claims["audience"] = "app.tetracube.red"
	claims["exp"] = tokenExpiration

	fmt.Println("Getting encryption key")
	var key interface{}
	var err error
	key, err = userController.loadData()
	if err != nil {
		return nil, fmt.Errorf("couldn't read key: %w", err)
	}

	fmt.Println("Building signing method")
	alg := jwt.GetSigningMethod("RS256")
	if alg == nil {
		return nil, fmt.Errorf("couldn't find signing method: %v", "RS256")
	}

	fmt.Println("Building the token")
	token := jwt.NewWithClaims(alg, claims)
	if k, ok := key.([]byte); !ok {
		return nil, fmt.Errorf("couldn't convert key data to key")
	} else {
		key, err = jwt.ParseRSAPrivateKeyFromPEM(k)
		if err != nil {
			return nil, err
		}
	}

	out, err := token.SignedString(key)
	if err != nil {
		return nil, err
	}
	return &out, nil
}

func (userController *UserController) loadData() ([]byte, error) {
	var rdr io.Reader
	if f, err := os.Open("/home/dave_cube/Projects/Tetracube_Red_Project/TetraCubeBackend/tetracube-gateway/privateKey.pem"); err == nil {
		rdr = f
		defer f.Close()
	} else {
		return nil, err
	}
	return io.ReadAll(rdr)
}
