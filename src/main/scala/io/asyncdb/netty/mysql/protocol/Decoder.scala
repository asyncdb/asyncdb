package io.asyncdb
package netty
package mysql

import cats._
import cats.syntax.all._
import io.netty.buffer.{ByteBuf, ByteBufUtil}
import io.netty.buffer.Unpooled
import java.nio.charset.Charset
import scala.annotation._
import scala.collection.mutable.ArrayBuffer
import protocol.server.Err

/**
 * Internal api, not fully safe, should carefully dealed at call site.
 * The buffer passed to this decoder is raw data buffer, mysql seq and len head is striped
 */
private[mysql] trait Decoder[V] {
  def decode(b: ByteBuf, charset: Charset): V

  def map[B](f: (V, Charset) => B): Decoder[B] = {
    val vd = this
    new Decoder[B] {
      def decode(b: ByteBuf, charset: Charset) = {
        f(vd.decode(b, charset), charset)
      }
    }
  }

  def map[B](f: V => B): Decoder[B] = {
    val vd = this
    new Decoder[B] {
      def decode(b: ByteBuf, charset: Charset) = {
        f(vd.decode(b, charset))
      }
    }
  }

  def flatMap[B](f: V => Decoder[B]): Decoder[B] = {
    val ve = this
    new Decoder[B] {
      def decode(b: ByteBuf, charset: Charset) = {
        val v = ve.decode(b, charset)
        f(v).decode(b, charset)
      }
    }
  }
}

object Decoder {

  def decode[V](buf: ByteBuf, charset: Charset)(implicit decoder: Decoder[V]) =
    Either.catchNonFatal {
      decoder.decode(buf, charset)
    }

  def pure[V](v: V): Decoder[V] = new Decoder[V] {
    def decode(buf: ByteBuf, charset: Charset) = {
      v
    }
  }

  private def decoderOf[V](f: (ByteBuf, Charset) => V): Decoder[V] =
    new Decoder[V] {
      def decode(buf: ByteBuf, charset: Charset) = f(buf, charset)
    }

  private def decoderOf[V](f: (ByteBuf) => V): Decoder[V] = new Decoder[V] {
    def decode(buf: ByteBuf, charset: Charset) = f(buf)
  }

  val int1: Decoder[Byte]   = decoderOf(_.readByte)
  val intL2: Decoder[Int]   = decoderOf(_.readShortLE)
  val intL3: Decoder[Int]   = decoderOf(_.readMediumLE)
  val intL4: Decoder[Int]   = decoderOf(_.readIntLE)
  val intL8: Decoder[Long]  = decoderOf(_.readLongLE)
  val uint1: Decoder[Short] = decoderOf(_.readUnsignedByte)

  def bytes(n: Int) = decoderOf { buf: ByteBuf =>
    val arr = Array.ofDim[Byte](n)
    buf.readBytes(arr)
    arr
  }

  def str(size: Int) = bytes(size).map { (bytes, charset) =>
    new String(bytes, charset)
  }

  val bytesEOF: Decoder[Array[Byte]] = decoderOf { buf =>
    val out = Array.ofDim[Byte](buf.readableBytes)
    buf.readBytes(out)
    out
  }

  val strEOF = bytesEOF.map { (bytes, charset) =>
    new String(bytes, charset)
  }

  val lenencInt = decoderOf { (buf, cs) =>
    val fb = buf.readByte
    (fb: @switch) match {
      case 0xFC => intL2.decode(buf, cs).toLong
      case 0xFD => intL3.decode(buf, cs).toLong
      case 0XFE => intL8.decode(buf, cs)
      case v    => v.toLong
    }
  }

  val ntBytes = decoderOf { buf: ByteBuf =>
    def readUntilZero(read: ArrayBuffer[Byte]): ArrayBuffer[Byte] = {
      val b = buf.readByte()
      if (b != '\u0000') {
        val after = read += b.toByte
        readUntilZero(after)
      } else read
    }
    val ab = readUntilZero(new ArrayBuffer[Byte])
    ab.toArray
  }

}

private[mysql] object PacketDecoder {
  def apply[V](implicit vd: Decoder[V]) = new PacketDecoder(vd)
  def isReady(buf: ByteBuf): Boolean = {
    @annotation.tailrec
    def isLastPacketReady(fromOffset: Int): Boolean = {
      val headReady = buf.readableBytes() >= fromOffset + 4
      headReady && {
        val packetLen   = buf.getUnsignedMediumLE(fromOffset)
        val packetReady = (buf.readableBytes() - fromOffset) >= packetLen + 4
        val isLast      = packetLen < Packet.MaxSize
        packetReady && (isLast || isLastPacketReady(fromOffset + packetLen + 4))
      }
    }
    isLastPacketReady(buf.readerIndex())
  }
}

private[mysql] final class PacketDecoder[V](md: Decoder[V]) {

  /**
   * Extract `payload` part of packet(s), and wrap them as composite view
   */
  @annotation.tailrec
  private def payloadBufs(
    buf: ByteBuf,
    from: Int,
    composite: Vector[ByteBuf]
  ): Vector[ByteBuf] = {
    val packetLen = buf.getUnsignedMediumLE(from)
    val dataStart = from + 4
    val dataEnd   = dataStart + packetLen
    val thisAdded = composite :+ buf.slice(dataStart, packetLen)
    if (packetLen >= Packet.MaxSize) {
      payloadBufs(buf, dataEnd, thisAdded)
    } else {
      // data will be consumed in slice(dataStart, packetLen), just mark the reader index here
      buf.readerIndex(dataEnd)
      thisAdded
    }
  }

  def decode(buf: ByteBuf, charset: Charset) = Either.catchNonFatal {
    val payloads = payloadBufs(buf, buf.readerIndex(), Vector.empty)
    val rawBuff  = Unpooled.wrappedBuffer(payloads.toArray: _*)
    md.decode(rawBuff, charset)
  }
}
