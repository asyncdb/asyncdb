package io.asyncdb
package nio
package mysql

import cats.data.NonEmptyList
import io.asyncdb.util.Hex
import org.scalatest._
import java.nio.ByteBuffer

class HandshakeInitSpec extends SocketSpec {

  val initBytes = Hex.toBytes(
    """0a352e352e352d31302e312e33312d4d617269614442002500000026712d277d614c3a00fff7e002003fa015000000000000000000007b2335234c376f4859687e61006d7973716c5f6e61746976655f70617373776f726400""")

  val handshakePacket =
    Packet(Int3(initBytes.size), Int1(0), ByteBuffer.wrap(initBytes))

  "HandshakeInit" - {
    "decode init packet" in {
      Codec.read[HandshakeInit](NonEmptyList.one(handshakePacket)) should be(
        'right)
    }
    "connect to server" in withSocket { socket =>
      socket.read[HandshakeInit](1000)
    }.unsafeToFuture.map { r =>
      r should be('right)
    }
  }
}
