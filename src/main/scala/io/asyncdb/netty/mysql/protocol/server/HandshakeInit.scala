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
) extends Message

case class ExtraHandshakeData(
  characterSet: Short,
  statusFlags: Int,
  capabilityFlagUpper: Int,
  authPluginDataLen: Byte,
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

  private def apply(b: BasicHandshakeData, e: ExtraHandshakeData) = {
     val c = CharsetMap.of(e.characterSet)
      new HandshakeInit(
        protocol = b.protocol,
        version = new String(b.version, c),
        connectionId = b.connectionId,
        authPluginData = b.authPluginDataPart1 ++ e.authPluginDataPart2,
        charset = c,
        cap = b.capabilityFlagLower & e.capabilityFlagUpper,
        authenticationMethod = new String(e.authenticationMethod, c)
      )
  }

  import Decoder._

  implicit val handshakeInitDecoder: Decoder[HandshakeInit] = {
    val basic = (int1 :: ntBytes :: int4 :: bytes(8) :: int1 :: int2)
      .as[BasicHandshakeData]
    val extra = (uint1 :: int2 :: int2 :: int1.flatMap { pdl =>
      val apd2Len = math.max(13, pdl - 8)
      Decoder.pure(pdl) :: bytes(10) :: bytes(apd2Len) :: ntBytes
    }).as[ExtraHandshakeData]
    for {
      b <- basic
      e <- extra
    } yield apply(b, e)
  }
}
