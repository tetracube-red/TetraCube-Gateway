package db

import (
	"database/sql"

	"github.com/uptrace/bun"
	"github.com/uptrace/bun/dialect/pgdialect"
	"github.com/uptrace/bun/driver/pgdriver"
)

type DatabaseConnection struct {
	DB *bun.DB
}

func Init() *DatabaseConnection {
	maxOpenConns := 8
	dsn := "postgres://tetracube_usr:changeme@localhost:5432/tetracube_db?sslmode=disable"

	sqldb := sql.OpenDB(pgdriver.NewConnector(pgdriver.WithDSN(dsn)))
	sqldb.SetMaxOpenConns(maxOpenConns)
	sqldb.SetMaxIdleConns(maxOpenConns)

	dbConn := bun.NewDB(sqldb, pgdialect.New())
	databaseConnection := DatabaseConnection{
		DB: dbConn,
	}
	return &databaseConnection
}
