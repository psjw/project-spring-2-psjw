DROP DATABASE IF EXISTS myapp;

CREATE DATABASE myapp;
USE myapp;

CREATE TABLE test (
                       id INTEGER AUTO_INCREMENT,
                       value TEXT,
                       PRIMARY KEY (id)
);