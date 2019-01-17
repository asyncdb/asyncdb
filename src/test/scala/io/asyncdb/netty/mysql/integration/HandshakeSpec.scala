package io.asyncdb
package netty
package mysql
package integration

import cats.effect._

class HandshakeSpec extends SocketSpec {
  "MySQLSocket" - {
    "handshake" in withSocket { socket =>
      IO.pure(succeed)
    }
  }
}
