package io.asyncdb
package nio
package mysql

import java.nio.charset.{Charset, StandardCharsets}
import codecs._

/**
 * https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeV10
 */
case class ExtraHandshakeData(
  characterSet: Int1,
  statusFlags: Int2,
  capabilityFlagUpper: Int2,
  authPluginDataLen: Int1,
  reserved: Array[Byte],
  authPluginDataPart2: Array[Byte]
)

case class HandshakeInit(
  protocol: Int1,
  version: String,
  connectionId: Int,
  authPluginData: Array[Byte],
  charset: Charset,
  cap: Int
)

object HandshakeInit {
  implicit val handshakeInitReader: Reader[HandshakeInit] = ???
}
