package io.asyncdb
package netty
package mysql
package protocol
package server

import io.netty.buffer.ByteBuf
import java.nio.charset.Charset
import scala.annotation._

trait ServerMessage extends Message

object ServerMessage {

  final object Headers {
    val Ok           = 0x00.toByte
    val Err          = 0xff.toByte
    val ComQuit      = 0x01.toByte
    val ComInitDB    = 0x02.toByte
    val ComQuery     = 0x03.toByte
    val ComFieldList = 0x04.toByte
    val HandshakeV10 = 0x0a.toByte
  }

  import Decoder._

  implicit val serverMsgDecoder: Decoder[ServerMessage] =
    new Decoder[ServerMessage] {

      def decodeMsg[A](buf: ByteBuf, charset: Charset)(
        implicit ae: Decoder[A]
      ) = {
        ae.decode(buf, charset)
      }

      def decode(buf: ByteBuf, charset: Charset) = {
        val head = buf.getByte(buf.readerIndex())
        (head: @switch) match {
          case Headers.HandshakeV10 =>
            decodeMsg[HandshakeInit](buf, charset)
          case Headers.Err =>
            decodeMsg[Err](buf, charset)
          case Headers.Ok =>
            decodeMsg[Ok](buf, charset)
          case _ => decodeMsg[Ok](buf, charset)
        }
      }
    }

}
