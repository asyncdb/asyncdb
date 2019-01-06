package io.asyncdb
package netty
package mysql
package protocol
package server

import io.netty.buffer._
import java.nio.charset.Charset
import cats.syntax.all._

/**
 * https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeV10
 */
case class HandshakeInit(
  protocol: Int,
  version: String,
  connectionId: Int,
  authPluginData: Array[Byte],
  charset: Charset,
  cap: Int,
  authenticationMethod: String
)

case class ExtraHandshakeData(
  characterSet: Short,
  statusFlags: Int,
  capabilityFlagUpper: Int,
  authPluginDataLen: Int,
  reserved: Array[Byte],
  authPluginDataPart2: Array[Byte],
  authenticationMethod: Array[Byte]
)

case class BasicHandshakeData(
  protocol: Byte,
  version: Array[Byte],
  connectionId: Int,
  authPluginDataPart1: Array[Byte],
  filter: Byte,
  capabilityFlagLower: Int
)

object HandshakeInit {
  import Decoder._
  implicit val handshakeInitDecoder: Decoder[HandshakeInit] = new Decoder[HandshakeInit] {
    def decode(buf: ByteBuf, charset: Charset) = {
      val basic = (int1 :: ntBytes :: int4 :: bytes(8) :: int1 :: int2).as[BasicHandshakeData]
      (uint1 :: int2 :: int2 :: int1 :: bytes(10))
      ???
    }

  }
}
