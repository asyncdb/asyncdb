package io.asyncdb
package netty
package mysql

import io.netty.buffer._
import java.nio.charset.Charset
import shapeless._

trait Encoder[A] {
  def encode(v: A, buf: ByteBuf, charset: Charset): Unit
  def contramap[B](f: B => A): Encoder[B] = {
    val ae = this
    new Encoder[B] {
      def encode(b: B, buf: ByteBuf, charset: Charset) = {
        ae.encode(f(b), buf, charset)
      }
    }
  }
}

object Encoder {

  val int1: Encoder[Int] = new Encoder[Int] {
    def encode(v: Int, buf: ByteBuf, charset: Charset) = {
      buf.ensureWritable(1)
      buf.writeByte(v)
    }
  }

  val int2: Encoder[Int] = new Encoder[Int] {
    def encode(v: Int, buf: ByteBuf, charset: Charset) = {
      buf.ensureWritable(2)
      buf.writeShortLE(v)
    }
  }

  val int3: Encoder[Int] = new Encoder[Int] {
    def encode(v: Int, buf: ByteBuf, charset: Charset) = {
      buf.ensureWritable(3)
      buf.writeMediumLE(v)
    }
  }

  val int4: Encoder[Int] = new Encoder[Int] {
    def encode(v: Int, buf: ByteBuf, charset: Charset) = {
      buf.ensureWritable(4)
      buf.writeIntLE(v)
    }
  }

  def bytes(n: Int): Encoder[Array[Byte]] = new Encoder[Array[Byte]] {
    def encode(v: Array[Byte], buf: ByteBuf, charset: Charset) = {
      buf.ensureWritable(v.size)
      buf.writeBytes(v)
    }
  }

  val ntText: Encoder[String] = new Encoder[String] {
    def encode(v: String, buf: ByteBuf, charset: Charset) = {
      val bytes = (v + "\u0000").getBytes(charset.name())
      buf.ensureWritable(bytes.size)
      buf.writeBytes(bytes)
    }
  }

  val hnil: Encoder[HNil] = new Encoder[HNil] {
    def encode(v: HNil, buf: ByteBuf, charset: Charset) = {

    }
  }
}

class PacketsEncoder[V] {

  @inline private def packet(seq: Int, len: Int, payload: ByteBuf) = {
    val headByts = Array.ofDim[Byte](4)
    val headerBuf = Unpooled.wrappedBuffer(headByts)
    headerBuf
      .writeByte(seq)
      .writeMediumLE(len)
    Unpooled.wrappedBuffer(headerBuf, payload)
  }

  def encodes[V](v: V, buf: ByteBuf, charset: Charset)(implicit ve: Encoder[V]) = {
    val fullBuf = ve.encode(v, buf, charset)
    val fullLength = buf.readableBytes()

    @scala.annotation.tailrec
    def splitPackets(from: Int, seq: Int, previous: Vector[ByteBuf]): Vector[ByteBuf] = {
      if(fullLength - from >= Packet.MaxSize) {
        val len = Packet.MaxSize
        val p = packet(seq, len, buf.slice(from, from + len))
        splitPackets(from + len, seq + 1, previous :+ p)
      } else {
        val len = fullLength - from
        val p = packet(seq, len, buf.slice(from, fullLength))
        previous :+ p
      }
    }
  }
}
