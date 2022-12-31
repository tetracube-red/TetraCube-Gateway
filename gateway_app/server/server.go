package server

import (
	"github.com/gin-gonic/gin"
	authenticationtoken "red.tetracube/tetracube-gateway/gateway_app/authentication_token"
	"red.tetracube/tetracube-gateway/gateway_app/db"
	"red.tetracube/tetracube-gateway/gateway_app/house"
	"red.tetracube/tetracube-gateway/gateway_app/user"
)

type Server struct {
	GinServerEngine *gin.Engine
	DbConnection    *db.DatabaseConnection
}

func Init(dbConnection *db.DatabaseConnection) *Server {
	s := Server{
		DbConnection: dbConnection,
	}
	s.NewRouter(dbConnection)
	s.GinServerEngine.Run(":8080")
	return &s
}

func (s *Server) NewRouter(dbConnection *db.DatabaseConnection) {
	s.GinServerEngine = gin.New()
	s.GinServerEngine.Use(gin.Logger())
	s.GinServerEngine.Use(gin.Recovery())

	houseRoutersGroup := s.GinServerEngine.Group("/house")
	{
		houseController := house.HouseController{DbConnection: dbConnection}
		houseRoutersGroup.POST("/create", houseController.CreateHouse)
	}

	authenticationTokenRoutersGroup := s.GinServerEngine.Group("/authentication-token")
	{
		authenticationTokenController := authenticationtoken.AuthenticationTokenController{DbConnection: dbConnection}
		authenticationTokenRoutersGroup.POST("/create", authenticationTokenController.CreateAuthenticationToken)
	}

	userRoutersGroup := s.GinServerEngine.Group("/users")
	{
		userController := user.UserController{DbConnection: dbConnection}
		userRoutersGroup.POST("/login", userController.UserLogin)
	}
}
