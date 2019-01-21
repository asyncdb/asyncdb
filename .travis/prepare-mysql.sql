CREATE DATABASE asyncdb;
CREATE USER asyncdb@'localhost' identified BY 'asyncdb';
GRANT ALL on asyncdb.* to asyncdb@'localhost';
