package io.asyncdb
package netty
package mysql

import io.netty.buffer.ByteBuf
import java.nio.charset.Charset
import shapeless._

package object protocol {
  implicit class ValueEncoderOps[A](val encoder: Encoder[A]) extends AnyVal {
    def ::[H](pre: Encoder[H]): Encoder[H :: A :: HNil] =
      new Encoder[H :: A :: HNil] {
        def encode(v: H :: A :: HNil, buf: ByteBuf, charset: Charset) = {
          val hv = v.head
          val av = v.tail.head
          pre.encode(hv, buf, charset)
          encoder.encode(av, buf, charset)
        }
      }
  }

  implicit class HListEncoderOps[L <: HList](val encoder: Encoder[L])
      extends AnyVal {
    def ::[H](pre: Encoder[H]): Encoder[H :: L] = new Encoder[H :: L] {
      def encode(v: H :: L, buf: ByteBuf, charset: Charset) = {
        pre.encode(v.head, buf, charset)
        encoder.encode(v.tail, buf, charset)
      }
    }
    def as[A](implicit gen: Generic.Aux[A, L]): Encoder[A] =
      encoder.contramap(l => gen.to(l))
  }

  implicit class ValueDecoderOps[A](val Decoder: Decoder[A]) extends AnyVal {
    def ::[H](pre: Decoder[H]): Decoder[H :: A :: HNil] =
      new Decoder[H :: A :: HNil] {
        def decode(buf: ByteBuf, charset: Charset) = {
          val h = pre.decode(buf, charset)
          val t = Decoder.decode(buf, charset)
          h :: t :: HNil
        }
      }
  }

  implicit class HListDecoderOps[L <: HList](val decoder: Decoder[L])
      extends AnyVal {
    def ::[H](pre: Decoder[H]): Decoder[H :: L] = new Decoder[H :: L] {
      def decode(buf: ByteBuf, charset: Charset) = {
        val h = pre.decode(buf, charset)
        val l = decoder.decode(buf, charset)
        h :: l
      }
    }
    def as[A](implicit gen: Generic.Aux[A, L]): Decoder[A] =
      decoder.map(l => gen.from(l))
  }
}
