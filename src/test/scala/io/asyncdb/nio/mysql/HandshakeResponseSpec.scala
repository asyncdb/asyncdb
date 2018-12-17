package io.asyncdb
package nio
package mysql
package packet
package client

import cats.effect.IO
import io.asyncdb.nio.mysql.packet.server.{HandshakeInit, OK}

class HandshakeResponseSpec extends SocketSpec {
  "HandshakeResponse" - {
    "connect to server" in withSocket { socket =>
      socket.read[HandshakeInit](1000).flatMap {
        case Left(ex) => IO(Left(ex))
        case Right(r) =>
          socket
            .write[HandshakeResponse](
              HandshakeResponseSpec.getHandshakeResponse(r),
              1000
            )
            .flatMap { _ =>
              println(s"Start reading ok")
              socket.read[OK](1000)
            }
      }
    }.unsafeToFuture.map { r =>
      println(r)
      r should be('right)
    }
  }

  object HandshakeResponseSpec {
    def getHandshakeResponse(hi: HandshakeInit) = HandshakeResponse(
      username = "test",
      charset = CharsetMap.Utf8_bin,
      seed = hi.authPluginData,
      authenticationMethod = hi.authenticationMethod,
      password = Some("hMCCUMe7RCthstbT"),
      database = Some("test")
    )
  }
}
