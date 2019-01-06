package io.asyncdb
package netty
package mysql

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.nio.charset.Charset
import scala.collection.mutable.ArrayBuffer

/**
 * Internal api, not fully safe, should carefully dealed at call site.
 * The buffer passed to this decoder is raw data buffer, mysql seq and len head is striped
 */
private[mysql] trait Decoder[V] {
  def decode(b: ByteBuf, charset: Charset): V
  def map[B](f: V => B): Decoder[B] = {
    val vd = this
    new Decoder[B] {
      def decode(b: ByteBuf, charset: Charset) = {
         f(vd.decode(b, charset))
      }
    }
  }
}

object Decoder {

  private def decoderOf[V](f: ByteBuf => V): Decoder[V] = new Decoder[V] {
    def decode(buf: ByteBuf, charset: Charset) = f(buf)
  }

  val int1: Decoder[Byte] = decoderOf(_.readByte)
  val int2: Decoder[Int] = decoderOf(_.readShortLE)
  val int3: Decoder[Int] = decoderOf(_.readMediumLE)
  val int4: Decoder[Int] = decoderOf(_.readIntLE)

  val uint1: Decoder[Short] = decoderOf[Short](_.readUnsignedByte)

  def bytes(n: Int) = decoderOf { buf: ByteBuf =>
    val arr = Array.ofDim[Byte](n)
    buf.readBytes(arr)
    arr
  }
  val ntBytes = decoderOf { buf: ByteBuf =>
    def readUntilZero(read: ArrayBuffer[Byte]): ArrayBuffer[Byte] = {
      val b = buf.readByte()
      if(b != '\u0000') {
        val after = read += b.toByte
        readUntilZero(after)
      } else read
    }
    val ab = readUntilZero(new ArrayBuffer[Byte])
    ab.toArray
  }

}

private[mysql] final class PacketDecoder[V](md: Decoder[V]){
  def isReady(buf: ByteBuf): Boolean = {
    @annotation.tailrec
    def isLastPacketReady(fromOffset: Int): Boolean = {
      val headReady = buf.readableBytes() < fromOffset + 4
      headReady && {
        val packetLen = buf.getUnsignedMedium(fromOffset + 1)
        val packetReady = (buf.readableBytes() - fromOffset) >= packetLen + 4
        val isLast = packetLen < Packet.MaxSize
        (isLast && packetReady) || isLastPacketReady(fromOffset + packetLen + 4)
      }
    }
    isLastPacketReady(buf.readerIndex())
  }

  @annotation.tailrec
  private def dataBuffer(buf: ByteBuf, from: Int, composite: Vector[ByteBuf]): Vector[ByteBuf] = {
    val packetLen = buf.getUnsignedMedium(from + 1)
    val dataStart = from + 4
    val dataEnd = dataStart + packetLen
    val thisAdded = composite :+ buf.slice(dataStart, dataEnd)
    if(packetLen >= Packet.MaxSize) {
      dataBuffer(buf, dataEnd, thisAdded)
    } else {
      thisAdded
    }
  }

  def decode(buf: ByteBuf, charset: Charset) = {
    val rawBuff = Unpooled.wrappedBuffer(dataBuffer(buf, buf.readerIndex(), Vector.empty).toArray: _*)
    md.decode(rawBuff, charset)
  }
}
