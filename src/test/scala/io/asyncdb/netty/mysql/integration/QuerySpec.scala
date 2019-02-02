package io.asyncdb
package netty
package mysql
package integration
import cats.effect.IO
import io.asyncdb.netty.mysql.protocol.client._
import io.asyncdb.netty.mysql.protocol.server._

class QuerySpec extends SocketSpec {
  val createTableNotExistsSql =
    "CREATE TABLE IF NOT EXISTS user(id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,name VARCHAR(32) NOT NULL);"
  val createTableSql =
    "CREATE TABLE user(id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,name VARCHAR(32) NOT NULL);"
  val insertUserSql     = "INSERT INTO user VALUES(null,'test');"
  val insertUserFailSql = "INSERT INTO user VALUES(null, null);"
  val selectUserSql     = "SELECT * FROM user where name = 'test'"

  val createTableSuccess = Query(createTableNotExistsSql)
  val createTableFail    = Query(createTableSql)
  val insertUserSuccess  = Query(insertUserSql)
  val insertUserFail     = Query(insertUserFailSql)
  val selectUserSuccess  = Query(selectUserSql)

  "MySQLSocket" - {
    "query" - {
//      "create table success" in withSocket { socket =>
//        socket
//          .write(createTableSuccess)
//          .flatMap(
//            _ =>
//              socket.read.map { r =>
//                r match {
//                  case ok: Ok =>
//                    succeed
//                  case _ =>
//                    fail
//                }
//              }
//          )
//      }
//      "create table fail" in withSocket { socket =>
//        socket
//          .write(createTableFail)
//          .flatMap(
//            _ =>
//              socket.read.attempt.map { r =>
//                r match {
//                  case Left(e: Err) =>
//                    e.errcode shouldEqual 1050
//                  case _ =>
//                    fail
//                }
//              }
//          )
//      }
    }
//    "insert" - {
//      "insert user success" in withSocket { socket =>
//        socket
//          .write(insertUserSuccess)
//          .flatMap(
//            _ =>
//              socket.read.map { r =>
//                r match {
//                  case ok: Ok =>
//                    succeed
//                  case _ =>
//                    fail
//                }
//              }
//          )
//      }
//      "insert user fail" in withSocket { socket =>
//        socket
//          .write(insertUserFail)
//          .flatMap(
//            _ =>
//              socket.read.attempt.map { r =>
//                r match {
//                  case Left(e: Err) =>
//                    e.errcode shouldEqual 1048
//                  case _ =>
//                    fail
//                }
//              }
//          )
//      }
//    }
    "select" - {
      "select user success" in withSocket { socket =>
        socket
          .write(selectUserSuccess)
          .flatMap(
            _ =>
              socket.read.map { r =>
                r match {
                  case ok: Ok =>
                    succeed
                  case _ =>
                    fail
                }
              }
          )
      }
    }
  }
}
