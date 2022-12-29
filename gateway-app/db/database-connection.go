package db

import (
	"database/sql"

	"github.com/uptrace/bun"
	"github.com/uptrace/bun/dialect/pgdialect"
	"github.com/uptrace/bun/driver/pgdriver"
)

type DatabaseConnection struct {
	db *bun.DB
}

func Init() *DatabaseConnection {
	dsn := "postgres://tetracube_usr:changeme@localhost:5432/tetracube_db?sslmode=disable"
	sqldb := sql.OpenDB(pgdriver.NewConnector(pgdriver.WithDSN(dsn)))

	dbConn := bun.NewDB(sqldb, pgdialect.New())
	databaseConnection := DatabaseConnection{
		db: dbConn,
	}
	return &databaseConnection
}
