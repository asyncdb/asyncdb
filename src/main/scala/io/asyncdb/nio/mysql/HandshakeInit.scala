package io.asyncdb
package nio
package mysql

import cats.syntax.all._
import java.nio.charset.{Charset, StandardCharsets}
import Reader.Unsafe

/**
 * https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeV10
 */
case class HandshakeInit(
  protocol: Byte,
  version: String,
  connectionId: Int,
  authPluginData: Array[Byte],
  charset: Charset,
  cap: Int
)

case class ExtraHandshakeData(
  characterSet: UInt1,
  statusFlags: Int2,
  capabilityFlagUpper: Int2,
  authPluginDataLen: Int1,
  reserved: Array[Byte],
  authPluginDataPart2: Array[Byte]
)

case class BasicHandshakeData(
  protocol: Int1,
  version: Array[Byte],
  connectionId: IntLE,
  authPluginDataPart1: Array[Byte],
  filter: Int1,
  capabilityFlagLower: Int2
)

object HandshakeInit {

  private val basicHandshakeDataReader: Reader[BasicHandshakeData] =
    Codec.reader { buf =>
      val p      = Unsafe.readInt1(buf)
      val v      = Unsafe.readNullEnded(buf)
      val cid    = Unsafe.readIntLE(buf)
      val apd1   = Unsafe.readN(buf, 8)
      val filter = Unsafe.readInt1(buf)
      val cfl    = Unsafe.readInt2(buf)
      BasicHandshakeData(p, v, cid, apd1, filter, cfl)
    }

  private val extraHandshakeDataReader: Reader[ExtraHandshakeData] =
    Codec.reader { buf =>
      val c        = Unsafe.readUInt1(buf)
      val sf       = Unsafe.readInt2(buf)
      val cfu      = Unsafe.readInt2(buf)
      val pdl      = Unsafe.readInt1(buf)
      val reserved = Unsafe.readN(buf, 10)
      val apd2     = Unsafe.readN(buf, math.max(13, pdl.value - 8))
      ExtraHandshakeData(
        characterSet = c,
        statusFlags = sf,
        capabilityFlagUpper = cfu,
        authPluginDataLen = pdl,
        reserved = reserved,
        authPluginDataPart2 = apd2
      )
    }

  implicit val handshakeInitReader: Reader[HandshakeInit] = {
    for {
      b <- basicHandshakeDataReader
      e <- extraHandshakeDataReader
    } yield {
      val c = CharsetMap.of(e.characterSet.value)
      HandshakeInit(
        protocol = b.protocol.value,
        version = new String(b.version, c),
        connectionId = b.connectionId.value,
        authPluginData = b.authPluginDataPart1 ++ e.authPluginDataPart2,
        charset = c,
        cap = b.capabilityFlagLower.value & e.capabilityFlagUpper.value
      )
    }
  }

}
