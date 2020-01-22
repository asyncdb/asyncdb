package io.asyncdb
package netty
package mysql
package protocol
package client

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
  authMethod: String
) extends ClientMessage

object HandshakeResponse {

  def apply(init: server.HandshakeInit, config: MySQLSocketConfig) = {
    val cap = {
      val base = Cap.baseCap
      val withDatabase =
        config.database.fold(base)(_ => base + Cap.ConnectWithDB)
      withDatabase
    }
    val passBytes = config.password.fold(Array.empty[Byte])(p =>
      Auth
        .nativePassword(init.authPluginData, p, CharsetMap.of(config.charset))
    )
    new HandshakeResponse(
      clientFlag = cap.mask,
      maxPacketSize = Packet.MaxSize,
      charset = config.charset,
      database = config.database,
      username = config.username,
      password = passBytes,
      authMethod = config.authMethod.getOrElse(init.authenticationMethod),
      filter = Array.fill(23)(0.toByte)
    )
  }

  import Encoder._

  implicit val handshakeResponseEncoder: Encoder[HandshakeResponse] =
    Encoder[HandshakeResponse] { data =>
      (intL4 :: intL4 :: uint1 :: bytes :: ntText :: lenencBytes :: ntText.? :: ntText)
        .as[HandshakeResponse]
    }

}
