package io.asyncdb
package nio
package mysql

import cats.data.NonEmptyList
import io.asyncdb.util.Hex
import org.scalatest._
import java.nio.ByteBuffer

class HandshakeInitSpec extends SocketSpec {




  "HandshakeInit" - {
    "decode init packet" in {
      Codec.read[HandshakeInit](HandshakeInit.MariaDB10) should be(
        'right)
      Codec.read[HandshakeInit](HandshakeInit.MySQL56) should be(
        'right)
    }
    "connect to server" in withSocket { socket =>
      socket.read[HandshakeInit](1000)
    }.unsafeToFuture.map { r =>
      r should be('right)
    }
  }


  object HandshakeInit {

    def packet(bytes: Array[Byte]) = {
      Packet(Int3(bytes.size), Int1(0), ByteBuffer.wrap(bytes))
    }

    val MariaDB10Bytes = Hex.toBytes(
      """0a352e352e352d31302e312e33312d4d617269614442002500000026712d277d614c3a00fff7e002003fa015000000000000000000007b2335234c376f4859687e61006d7973716c5f6e61746976655f70617373776f726400""")
    val MySQL56Bytes = Hex.toBytes(
      """0a352e372e31332d6c6f6700160c0000533f5d042025172900ffff210200ffc1150000000000000000000027105a290c1f3a71111b5b68006d7973716c5f6e61746976655f70617373776f726400""")

    val MariaDB10 = NonEmptyList.one(packet(MariaDB10Bytes))
    val MySQL56 = NonEmptyList.one(packet(MySQL56Bytes))


  }
}
