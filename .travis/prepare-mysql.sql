CREATE DATABASE asyncdb_test;
CREATE USER asyncdb_test_user identified BY 'async_test_user_pass';
GRANT ALL on asyncdb_test.* to asyncdb_test_user;
