package server

import (
	"github.com/gin-gonic/gin"
	"red.tetracube/tetracube-gateway/gateway-app/db"
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
}
