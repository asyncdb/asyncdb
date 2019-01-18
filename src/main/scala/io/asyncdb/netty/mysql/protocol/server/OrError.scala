package io.asyncdb
package netty
package mysql
package protocol
package server

import io.netty.buffer.ByteBuf
import java.nio.charset.Charset

case class OrErr[R <: Message](value: Either[Err, R]) extends Message

object OrErr {
  implicit def resultOrErr[R <: Message](
    implicit ad: Decoder[R]
  ): Decoder[OrErr[R]] = new Decoder[OrErr[R]] {
    def decode(buf: ByteBuf, charset: Charset) = {
      val head = buf.getByte(buf.readerIndex())
      if (head.toByte == -1) {
        OrErr(Left(Err.errDecoder.decode(buf, charset)))
      } else {
        OrErr(Right(ad.decode(buf, charset)))
      }
    }
  }

}
