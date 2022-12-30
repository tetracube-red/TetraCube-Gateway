package main

import (
	"fmt"

	"red.tetracube/tetracube-gateway/gateway_app/db"
	"red.tetracube/tetracube-gateway/gateway_app/server"
)

func main() {
	fmt.Println("Getting database connection")
	dbConnection := db.Init()
	server.Init(dbConnection)
}
