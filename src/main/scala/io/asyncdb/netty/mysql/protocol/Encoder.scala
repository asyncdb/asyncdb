package io.asyncdb
package netty
package mysql

import io.netty.buffer._
import java.nio.charset.Charset
import shapeless._

trait Encoder[A] {

  def encode(v: A, buf: ByteBuf, charset: Charset): Unit

  def productL[B](f: A => B, e: Encoder[B]): Encoder[A] = {
    val ae = this
    new Encoder[A] {
      def encode(a: A, buf: ByteBuf, charset: Charset) = {
        e.encode(f(a), buf, charset)
        ae.encode(a, buf, charset)
      }
    }
  }

  def * : Encoder[Seq[A]] = {
    val ae = this
    new Encoder[Seq[A]] {
      def encode(v: Seq[A], buf: ByteBuf, charset: Charset) = {
        v.foreach { a =>
          ae.encode(a, buf, charset)
        }
      }
    }
  }

  def contramap[B](f: B => A): Encoder[B] = {
    val ae = this
    new Encoder[B] {
      def encode(b: B, buf: ByteBuf, charset: Charset) = {
        ae.encode(f(b), buf, charset)
      }
    }
  }

  def ? : Encoder[Option[A]] = {
    val ae = this
    new Encoder[Option[A]] {
      def encode(v: Option[A], buf: ByteBuf, charset: Charset) = {
        v.foreach { vv =>
          ae.encode(vv, buf, charset)
        }
      }
    }
  }

}

object Encoder {

  def apply[A](f: A => Encoder[A]): Encoder[A] = new Encoder[A] {
    def encode(a: A, buf: ByteBuf, charset: Charset) = {
      f(a).encode(a, buf, charset)
    }
  }

  val int1: Encoder[Byte] = new Encoder[Byte] {
    def encode(v: Byte, buf: ByteBuf, charset: Charset) = {
      buf.ensureWritable(1)
      buf.writeByte(v & 0xf)
    }
  }

  val intL2: Encoder[Int] = new Encoder[Int] {
    def encode(v: Int, buf: ByteBuf, charset: Charset) = {
      buf.ensureWritable(2)
      buf.writeShortLE(v)
    }
  }

  val intL3: Encoder[Int] = new Encoder[Int] {
    def encode(v: Int, buf: ByteBuf, charset: Charset) = {
      buf.ensureWritable(3)
      buf.writeMediumLE(v)
    }
  }

  val intL4: Encoder[Int] = new Encoder[Int] {
    def encode(v: Int, buf: ByteBuf, charset: Charset) = {
      buf.ensureWritable(4)
      buf.writeIntLE(v)
    }
  }

  val intL8: Encoder[Long] = new Encoder[Long] {
    def encode(v: Long, buf: ByteBuf, charset: Charset) = {
      buf.ensureWritable(8)
      buf.writeLongLE(v)
    }
  }

  def noop[A]: Encoder[A] = new Encoder[A] {
    def encode(a: A, buf: ByteBuf, charset: Charset) = {}
  }

  val lenencInt: Encoder[Long] = new Encoder[Long] {
    def encode(v: Long, buf: ByteBuf, charset: Charset) = {
      if (v < 251) {
        buf.ensureWritable(1)
        buf.writeByte(v.toByte)
      } else if (v < 0x100000) {
        buf.ensureWritable(2)
        buf.writeByte(0xFC)
        intL2.encode(v.toInt, buf, charset)
      } else if (v < 0x1000000) {
        buf.ensureWritable(3)
        buf.writeByte(0xFD)
        intL3.encode(v.toInt, buf, charset)
      } else {
        buf.ensureWritable(8)
        buf.writeByte(0xFE)
        intL8.encode(v, buf, charset)
      }
    }
  }

  val lenencBytes: Encoder[Array[Byte]] = new Encoder[Array[Byte]] {
    def encode(ba: Array[Byte], buf: ByteBuf, charset: Charset) = {
      val l = ba.size
      lenencInt.encode(l, buf, charset)
      bytes.encode(ba, buf, charset)
    }
  }

  val lenencText: Encoder[String] = new Encoder[String] {
    def encode(v: String, buf: ByteBuf, charset: Charset) = {
      val charBytes = v.getBytes(charset)
      val l         = charBytes.size
      lenencInt.encode(l, buf, charset)
      bytes.encode(charBytes, buf, charset)
    }
  }

  val uint1: Encoder[Short] = new Encoder[Short] {
    def encode(v: Short, buf: ByteBuf, charset: Charset) = {
      buf.ensureWritable(1)
      buf.writeByte(v)
    }
  }

  val bytes: Encoder[Array[Byte]] = new Encoder[Array[Byte]] {
    def encode(v: Array[Byte], buf: ByteBuf, charset: Charset) = {
      buf.ensureWritable(v.size)
      buf.writeBytes(v)
    }
  }

  val ntText: Encoder[String] = new Encoder[String] {
    def encode(v: String, buf: ByteBuf, charset: Charset) = {
      val bytes = v.getBytes(charset)
      buf.ensureWritable(bytes.size + 1)
      buf.writeBytes(bytes)
      buf.writeByte(0x00)
    }
  }

}

object PacketsEncoder {

  @inline private def packet(seq: Int, len: Int, payload: ByteBuf) = {
    val headByts  = Array.ofDim[Byte](4)
    val headerBuf = Unpooled.wrappedBuffer(headByts)
    headerBuf.clear
    headerBuf
      .writeMediumLE(len)
      .writeByte(seq)
    Unpooled.wrappedBuffer(headerBuf, payload)
  }

  def encode[V](
    v: V,
    buf: ByteBuf,
    charset: Charset
  )(
    implicit ve: Encoder[V]
  ) = {
    val fullBuf    = ve.encode(v, buf, charset)
    val fullLength = buf.readableBytes()

    println("---------")
    println(HexDump.dump(buf))

    @scala.annotation.tailrec
    def splitPackets(
      from: Int,
      seq: Int,
      previous: Vector[ByteBuf]
    ): Vector[ByteBuf] = {
      if (fullLength - from >= Packet.MaxSize) {
        val len = Packet.MaxSize
        val p   = packet(seq, len, buf.slice(from, len))
        splitPackets(from + len, seq + 1, previous :+ p)
      } else {
        val len = fullLength - from
        val p   = packet(seq, len, buf.slice(from, len))
        previous :+ p
      }
    }
    splitPackets(0, 1, Vector.empty)
  }
}
