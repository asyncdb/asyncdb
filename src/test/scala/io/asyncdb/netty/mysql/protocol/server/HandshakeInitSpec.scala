package io.asyncdb
package netty
package mysql
package protocol
package server

import cats.effect._
import io.netty.buffer.Unpooled
import java.nio.charset.Charset

class HandshakeInitSpec extends SocketSpec {

  val CS = Charset.defaultCharset()

  "HandshakeInit" - {
    "decode init packet" in {
      Decoder.decode[HandshakeInit](HandshakeInit.MariaDB10, CS) should be('right)
      Decoder.decode[HandshakeInit](HandshakeInit.MySQL56, CS) should be('right)
    }
    "connect to server" in withSocket { socket =>
      socket.read.map { init =>
        println(init)
        init should be(an[HandshakeInit])
      }
    }
  }

  object HandshakeInit {

    val MariaDB10Bytes = HexDump.decode(
      """0a352e352e352d31302e312e33312d4d617269614442002500000026712d277d614c3a00fff7e002003fa015000000000000000000007b2335234c376f4859687e61006d7973716c5f6e61746976655f70617373776f726400"""
    )
    val MySQL56Bytes = HexDump.decode(
      """0a352e372e31332d6c6f6700160c0000533f5d042025172900ffff210200ffc1150000000000000000000027105a290c1f3a71111b5b68006d7973716c5f6e61746976655f70617373776f726400"""
    )

    val MariaDB10 = Unpooled.wrappedBuffer(MariaDB10Bytes)
    val MySQL56 = Unpooled.wrappedBuffer(MySQL56Bytes)
  }
}
