package io.asyncdb
package netty
package mysql
package integration

import cats.effect._
import io.asyncdb.netty.mysql.protocol.server._

class HandshakeSpec extends SocketSpec {

  val NoPass          = config.copy(username = "root", password = None)
  val InvalidDatabase = config.copy(database = Some("invalid_database"))
  val InvalidUser     = config.copy(username = "invalid_user")
  val InvalidPass =
    config.copy(username = "asyncdb_test_user", password = Some("asyncdb"))

  "MySQLSocket" - {
    "handshake" - {
      "With user and pass" in withSocket { socket =>
        IO.pure(succeed)
      }
      "With no pass" in withSocket(NoPass) { socket =>
        IO.pure(succeed)
      }
      "Invalid database" in {
        withSocket(InvalidDatabase)(_ => IO.unit).attempt.map { r =>
          r match {
            case Left(e: Err) =>
              e.errcode shouldEqual 1044
            case _ =>
              fail
          }
        }
      }
      "Invalid user" in {
        withSocket(InvalidUser)(_ => IO.unit).attempt.map { r =>
          r match {
            case Left(e: Err) =>
              e.errcode shouldEqual 1045
            case _ =>
              fail
          }
        }
      }
      "Invalid pass" in {
        withSocket(InvalidUser)(_ => IO.unit).attempt.map { r =>
          r match {
            case Left(e: Err) =>
              e.errcode shouldEqual 1045
            case _ =>
              fail
          }
        }
      }
    }
  }
}
