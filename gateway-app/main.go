package main

import (
	"fmt"

	"red.tetracube/tetracube-gateway/gateway-app/db"
	"red.tetracube/tetracube-gateway/gateway-app/server"
)

func main() {
	fmt.Println("Getting database connection")
	dbConnection := db.Init()
	server.Init(dbConnection)
}
