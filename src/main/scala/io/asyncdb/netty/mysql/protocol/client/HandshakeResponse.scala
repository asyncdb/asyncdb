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
  response: Array[Byte],
  database: String,
  authMethod: String,
  attrs: Seq[(String, String)]
) extends Message

object HandshakeResponse {

  def apply(
    init: server.HandshakeInit,
    charset: Short,
    database: String,
    username: String,
    password: String) = {

    new HandshakeResponse(
      clientFlag = Cap.baseCap.mask,
      maxPacketSize = Packet.MaxSize,
      charset = charset,
      database = database,
      username = username,
      response = Auth.nativePassword(init.authPluginData, password, CharsetMap.of(charset)),
      authMethod = init.authenticationMethod,
      attrs = Seq.empty,
      filter = Array.fill(23)(0.toByte)
    )
  }

  import Encoder._

  implicit val handshakeResponseEncoder: Encoder[HandshakeResponse] =
    Encoder[HandshakeResponse] { data =>
      val resp =
        if(Cap(data.clientFlag).has(Cap.PluginAuthLenencData))
          lenencBytes
        else lenencBytes.productL(_.size.toLong, lenencInt)
      val attrs = (lenencText :: lenencText).contramap[(String, String)] {
        case (k, v) => k :: v :: HNil
      }.*.productL(l => l.size.toLong, lenencInt)
      (intL4 :: intL4 :: uint1 :: bytes :: ntText :: resp :: ntText :: ntText :: attrs).as[HandshakeResponse]
    }

}
