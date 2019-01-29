package io.asyncdb
package netty
package mysql
package protocol
package client

import io.netty.buffer._
import java.nio.charset.Charset

trait ClientMessage extends Message

object ClientMessage {
  implicit val clientMessageEncoder: Encoder[ClientMessage] = new Encoder[ClientMessage] {

    private def encodeMsg[V](v: V, buf: ByteBuf, cs: Charset)(implicit ve: Encoder[V]) = ve.encode(v, buf, cs)

    def encode(v: ClientMessage, buf: ByteBuf, charset: Charset) = v match {
      case m: HandshakeResponse  => encodeMsg[HandshakeResponse](m, buf, charset)
    }
  }
}
