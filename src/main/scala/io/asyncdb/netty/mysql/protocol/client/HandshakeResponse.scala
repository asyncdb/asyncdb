package io.asyncdb
package netty
package mysql
package protocol
package client

import io.netty.buffer.ByteBuf
import java.nio.charset.Charset
import shapeless._

/**
 * HandshakeResponse41
 */
case class HandshakeResponse(
  clientFlag: Int,
  maxPacketSize: Int,
  charset: Short,
  filter: Array[Byte],
  username: String,
  password: Array[Byte],
  database: Option[String],
  authMethod: Option[String]
) extends Message

object HandshakeResponse {

  def apply(init: server.HandshakeInit, config: MySQLSocketConfig) = {
    val cap = {
      val base = Cap.baseCap
      val withAuthPlugin = config.authMethod.fold(base)(_ => base + Cap.PluginAuth)
      val withDatabase = config.database.fold(withAuthPlugin)(_ => base + Cap.ConnectWithDB)
      withDatabase
    }
    val passBytes = config.password.fold(Array.empty[Byte])(p => Auth.nativePassword(init.authPluginData, p, CharsetMap.of(config.charset)))
    new HandshakeResponse(
      clientFlag = cap.mask,
      maxPacketSize = Packet.MaxSize,
      charset = config.charset,
      database = config.database,
      username = config.username,
      password = passBytes,
      authMethod = config.authMethod,
      filter = Array.fill(23)(0.toByte)
    )
  }

  import Encoder._

  implicit val handshakeResponseEncoder: Encoder[HandshakeResponse] =
    Encoder[HandshakeResponse] { data =>
      (intL4 :: intL4 :: uint1 :: bytes :: ntText :: lenencBytes :: ntText.? :: ntText.?).as[HandshakeResponse]
    }

}
