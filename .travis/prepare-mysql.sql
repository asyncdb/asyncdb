CREATE DATABASE asyncdb_test;
CREATE USER asyncdb_test_user@'localhost' identified BY 'asyncdb_test_user_pass';
GRANT ALL on asyncdb_test.* to asyncdb_test_user@'localhost';
