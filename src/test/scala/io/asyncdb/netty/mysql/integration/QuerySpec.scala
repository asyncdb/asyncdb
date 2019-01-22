package io.asyncdb
package netty
package mysql
package integration
import cats.effect.IO

import io.asyncdb.netty.mysql.protocol.client._

class QuerySpec extends SocketSpec {
  val createTable =
    "CREATE TABLE IF NOT EXISTS user(id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,name VARCHAR(32) NOT NULL);"
  val query = Query(createTable)
  "MySQLSocket" - {
    "query" - {
      "create table" in withSocket { socket =>
        socket.write(query).map(_ => succeed)
//        IO.pure(succeed)
      }
    }
  }

}
